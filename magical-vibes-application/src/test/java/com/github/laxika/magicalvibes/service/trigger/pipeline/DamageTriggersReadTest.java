package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import com.github.laxika.magicalvibes.service.trigger.pipeline.DamageTriggers.Read;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** How the damage collectors read a damage ability for the condition they watch: {@link DamageTriggers.Read}. */
class DamageTriggersReadTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);
    private static final PermanentPredicate DRAGON = new PermanentHasSubtypePredicate(CardSubtype.DRAGON);

    @Test
    void theReadGivesTheFilterAndTheLimitAndWrapsTheInterveningIfAroundTheEffect() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureDealtDamage().withFilter(DRAGON), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), DRAW);

        List<Read> reads = Read.read(WatchedAbilities.printed(card, Triggers.creatureDealtDamage(), Zone.BATTLEFIELD),
                Triggers.creatureDealtDamage());

        assertThat(reads).singleElement().satisfies(read -> {
            assertThat(read.effect()).isEqualTo(new ConditionalEffect(new ControllerTurn(), DRAW));
            assertThat(read.filter()).isEqualTo(DRAGON);
            assertThat(read.limit()).isEqualTo(TriggerLimit.DO_ONLY_ONCE_EACH_TURN);
            assertThat(read.eachSource()).isFalse();
        });
    }

    @Test
    void triggersOnlyOnceEachTurnIsMarkedByTheAbilityOnItsSource() {
        Card card = new Card();
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), GAIN);
        List<Read> reads = Read.read(Read.printed(card, Triggers.dealtDamage()), Triggers.dealtDamage());
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        reads.getFirst().markLimit(gameData, sourceId);

        assertThat(limitReached(gameData, reads.getFirst(), sourceId)).isTrue();
        assertThat(limitReached(gameData, reads.getFirst(), UUID.randomUUID())).isFalse();
        assertThat(limitReached(gameData, reads.get(1), sourceId)).isFalse();
    }

    @Test
    void eachSourceIsTheMultiplicity() {
        Card card = new Card();
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE, DRAW);

        Read eachSource = Read.of(Read.printed(card, Triggers.dealtDamage()).getFirst(), Triggers.dealtDamage());

        assertThat(eachSource.eachSource()).isTrue();
        assertThat(eachSource.effect()).isEqualTo(DRAW);
    }

    @Test
    void theStrictReadRejectsAFilterALimitOrAMultiplicity() {
        Card filtered = new Card();
        filtered.addTrigger(Triggers.dealtDamage().withFilter(DRAGON), DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.dealtDamage(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE), DRAW);
        Card eachSource = new Card();
        eachSource.addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE, DRAW);
        Card plain = new Card();
        plain.addTrigger(Triggers.dealtDamage(), DRAW);

        assertThatThrownBy(() -> Read.effects(Read.printed(filtered, Triggers.dealtDamage()), Triggers.dealtDamage()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Read.effects(Read.printed(limited, Triggers.dealtDamage()), Triggers.dealtDamage()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Read.effects(Read.printed(eachSource, Triggers.dealtDamage()),
                Triggers.dealtDamage())).isInstanceOf(IllegalStateException.class);
        assertThat(Read.effects(Read.printed(plain, Triggers.dealtDamage()), Triggers.dealtDamage()))
                .containsExactly(DRAW);
        assertThat(Read.effects(Read.printed(filtered, Triggers.dealtDamage()), Triggers.dealtDamage(),
                filter -> false)).isEmpty();
        assertThat(Read.effects(Read.printed(filtered, Triggers.dealtDamage()), Triggers.dealtDamage(),
                DRAGON::equals)).containsExactly(DRAW);
    }

    @Test
    void anAbilityWithSeveralDamageConditionsIsReadForEachWithItsOwnFilter() {
        PermanentPredicate pirate = new PermanentHasSubtypePredicate(CardSubtype.PIRATE);
        Card card = new Card();
        card.addTrigger(List.of(Triggers.sourceYouControlDealsDamageToPlayer().withFilter(DRAGON),
                Triggers.creatureYouControlDealsDamageToPlaneswalker().withFilter(pirate)), DRAW);

        assertThat(card.getTriggeredAbilities()).hasSize(1);
        assertThat(Read.read(Read.printed(card, Triggers.sourceYouControlDealsDamageToPlayer()),
                Triggers.sourceYouControlDealsDamageToPlayer())).singleElement().extracting(Read::filter)
                .isEqualTo(DRAGON);
        assertThat(Read.read(Read.printed(card, Triggers.creatureYouControlDealsDamageToPlaneswalker()),
                Triggers.creatureYouControlDealsDamageToPlaneswalker())).singleElement().extracting(Read::filter)
                .isEqualTo(pirate);
    }

    @Test
    void grantedAbilitiesAreFoundByTheWatchedTriggerAndReadWithTheirFilter() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.dealsDamage().withFilter(DRAGON), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThat(WatchedAbilities.temporary(permanent, Triggers.dealsDamage())).containsExactly(ability);
        assertThat(Read.read(WatchedAbilities.temporary(permanent, Triggers.dealsDamage()), Triggers.dealsDamage()))
                .singleElement().extracting(Read::filter).isEqualTo(DRAGON);
    }

    @Test
    void theAllyCombatDamageReadTakesTheFilterAndOneOrMoreFromTheAbility() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), DRAW);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(DRAGON),
                TriggerOptions.ONE_OR_MORE.withInterveningIf(new ControllerTurn()), GAIN);

        List<Read> reads = Read.read(Read.printed(card, Triggers.creatureYouControlDealsCombatDamageToPlayer()),
                Triggers.creatureYouControlDealsCombatDamageToPlayer());

        assertThat(reads).extracting(Read::filter).containsExactly(null, DRAGON);
        assertThat(reads).extracting(Read::oneOrMore).containsExactly(false, true);
        assertThat(reads).extracting(Read::effect)
                .containsExactly(DRAW, new ConditionalEffect(new ControllerTurn(), GAIN));
    }

    @Test
    void aCombatConditionGivesItsFilter() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(DRAGON), DRAW);

        assertThat(Read.read(Read.printed(card, Triggers.creatureYouControlDealsCombatDamageToPlayer()),
                Triggers.creatureYouControlDealsCombatDamageToPlayer())).singleElement().extracting(Read::filter)
                .isEqualTo(DRAGON);
    }

    private static boolean limitReached(GameData gameData, Read read, UUID sourceId) {
        return TriggeredAbilityStackService.limitReached(gameData, read.limit(), sourceId, read.ability().limitKey());
    }
}

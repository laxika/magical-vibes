package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.l.LanternSpirit;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AntiquitiesOnTheLooseTest extends BaseCardTest {

    private List<Permanent> spiritTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .toList();
    }

    private List<Permanent> spiritsYouControl() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();
    }

    @Test
    @DisplayName("Has Spirit token creation, not-from-hand counter effect, and flashback cost")
    void hasCorrectEffects() {
        AntiquitiesOnTheLoose card = new AntiquitiesOnTheLoose();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect token = (CreateTokenEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(token.amount()).isEqualTo(new Fixed(2));
        assertThat(token.tokenName()).isEqualTo("Spirit");
        assertThat(token.power()).isEqualTo(2);
        assertThat(token.toughness()).isEqualTo(2);
        assertThat(token.color()).isEqualTo(CardColor.WHITE);
        assertThat(token.colors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(token.subtypes()).containsExactly(CardSubtype.SPIRIT);

        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ConditionalEffect.class);
        ConditionalEffect conditional =
                (ConditionalEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(conditional.wrapped()).isInstanceOf(PutCounterOnEachControlledPermanentEffect.class);
        PutCounterOnEachControlledPermanentEffect counters =
                (PutCounterOnEachControlledPermanentEffect) conditional.wrapped();
        assertThat(counters.predicate()).isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT));

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{4}{W}{W}");
    }

    @Test
    @DisplayName("Normal cast creates two 2/2 red and white Spirit tokens without counters")
    void normalCastCreatesSpiritsWithoutCounters() {
        harness.setHand(player1, List.of(new AntiquitiesOnTheLoose()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> spirits = spiritTokens();
        assertThat(spirits).hasSize(2);
        for (Permanent spirit : spirits) {
            assertThat(spirit.getCard().getPower()).isEqualTo(2);
            assertThat(spirit.getCard().getToughness()).isEqualTo(2);
            assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }
    }

    @Test
    @DisplayName("Flashback creates Spirit tokens with +1/+1 counters on each Spirit you control")
    void flashbackCreatesSpiritsWithCounters() {
        harness.setGraveyard(player1, List.of(new AntiquitiesOnTheLoose()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        List<Permanent> spirits = spiritTokens();
        assertThat(spirits).hasSize(2);
        for (Permanent spirit : spirits) {
            assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Flashback puts +1/+1 counters on Spirits already on the battlefield")
    void flashbackCountersExistingSpirits() {
        harness.setGraveyard(player1, List.of(new AntiquitiesOnTheLoose()));
        Permanent existingSpirit = harness.addToBattlefieldAndReturn(player1, new LanternSpirit());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(spiritTokens()).hasSize(2);
        assertThat(spiritsYouControl()).hasSize(3);
        assertThat(existingSpirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        for (Permanent spirit : spiritsYouControl()) {
            assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Flashback exiles Antiquities on the Loose after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new AntiquitiesOnTheLoose()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Antiquities on the Loose"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Antiquities on the Loose"));
    }

    @Test
    @DisplayName("Flashback puts the sorcery on stack as cast from graveyard")
    void flashbackPutsSpellOnStackFromGraveyard() {
        harness.setGraveyard(player1, List.of(new AntiquitiesOnTheLoose()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Antiquities on the Loose");
        assertThat(entry.isCastWithFlashback()).isTrue();
        assertThat(entry.getSourceZone()).isEqualTo(Zone.GRAVEYARD);
    }
}

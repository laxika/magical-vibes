package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AninaNaturalParallelist.class, Fireball.class, GrizzlyBears.class})
class AninaNaturalParallelistTest extends BaseCardTest {

    @Test
    void castsXSpellAndConjuresCreatureWithMatchingManaValue() {
        Permanent anina = addReadyAnina();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        List<Card> conjuredCards = Stream.concat(
                        gd.playerBattlefields.get(player1.getId()).stream()
                                .filter(permanent -> !permanent.getId().equals(anina.getId()))
                                .map(Permanent::getCard),
                        gd.playerGraveyards.get(player1.getId()).stream())
                .filter(card -> !(card instanceof Fireball))
                .toList();
        assertThat(conjuredCards).singleElement().satisfies(card ->
                assertThat(card.getManaValue()).isEqualTo(1));
    }

    @Test
    void tapAbilityAddsGreenAndBlueManaRestrictedToXSpells() {
        addReadyAnina();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getXSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getXSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCannotCastSpellWithoutX() {
        addReadyAnina();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingSpellWithoutXDoesNotTriggerConjuring() {
        addReadyAnina();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentsXSpellDoesNotTriggerConjuring() {
        addReadyAnina();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player2, 0, 1, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void choosingZeroForXStillTriggersConjuring() {
        addReadyAnina();
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void bothRestrictedManaCanPayForTheSameXSpell() {
        Permanent anina = addReadyAnina();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 2, player2.getId());

        assertThat(anina.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getXSpellOnlyManaTotal()).isZero();
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void summoningSickAninaCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new AninaNaturalParallelist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getXSpellOnlyManaTotal()).isZero();
    }

    private Permanent addReadyAnina() {
        return addCreatureReady(player1, new AninaNaturalParallelist());
    }
}

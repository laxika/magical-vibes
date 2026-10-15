package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Imagecrafter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakishCrew.class, DauthiMercenary.class, GrizzlyBears.class, Shock.class, Imagecrafter.class})
class RakishCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mercenary token")
    void createsMercenaryToken() {
        castRakishCrew();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("The Mercenary token boosts a creature you control at sorcery speed")
    void mercenaryBoostsCreatureYouControl() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRakishCrew();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When an outlaw you control dies, each opponent loses 1 life and you gain 1 life")
    void outlawDeathDrainsOpponent() {
        castRakishCrew();
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());

        destroyWithShock(outlaw);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger when a non-outlaw creature you control dies")
    void nonOutlawDeathDoesNotTrigger() {
        castRakishCrew();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithShock(creature);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A Mercenary token dying triggers the drain")
    void mercenaryTokenDeathDrainsOpponent() {
        castRakishCrew();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();

        destroyWithShock(mercenary);

        assertThat(findPermanents(player1, "Mercenary")).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent's outlaw dying does not trigger the drain")
    void opposingOutlawDeathDoesNotTrigger() {
        castRakishCrew();
        Permanent outlaw = harness.addToBattlefieldAndReturn(player2, new DauthiMercenary());

        destroyWithShock(outlaw);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Mercenary cannot activate outside a main phase")
    void mercenaryCannotActivateDuringCombat() {
        castRakishCrew();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, mercenaryIndex, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature that became an outlaw triggers the drain when it dies")
    void creatureMadeIntoOutlawDrainsOpponent() {
        castRakishCrew();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        changeCreatureType(creature, CardSubtype.ROGUE);

        destroyWithShock(creature);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An outlaw that lost its outlaw types does not trigger the drain")
    void creatureMadeIntoNonOutlawDoesNotDrain() {
        castRakishCrew();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        changeCreatureType(creature, CardSubtype.BEAR);

        destroyWithShock(creature);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void changeCreatureType(Permanent target, CardSubtype subtype) {
        Permanent imagecrafter = harness.addToBattlefieldAndReturn(player1, new Imagecrafter());
        imagecrafter.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int imagecrafterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(imagecrafter);
        harness.activateAbility(player1, imagecrafterIndex, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(subtype);
    }

    private void castRakishCrew() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RakishCrew()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}

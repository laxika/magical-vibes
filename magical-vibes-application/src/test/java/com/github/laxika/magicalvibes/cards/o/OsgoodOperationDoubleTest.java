package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SonicScrewdriver;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OsgoodOperationDouble.class, GrizzlyBears.class, SonicScrewdriver.class})
class OsgoodOperationDoubleTest extends BaseCardTest {

    @Test
    void castTriggerCreatesNonlegendaryTokenCopy() {
        harness.setHand(player1, List.of(new OsgoodOperationDouble()));
        addOsgoodMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> copies = findPermanents(player1, "Osgood, Operation Double").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    void paradoxInvestigatesForSpellCastFromExileButNotFromHand() {
        harness.addToBattlefield(player1, new OsgoodOperationDouble());
        GrizzlyBears exiledSpell = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, exiledSpell.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void activatedManaIsRestrictedToArtifacts() {
        Permanent osgood = addCreatureReady(player1, new OsgoodOperationDouble());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player1, List.of(new SonicScrewdriver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(osgood.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sonic Screwdriver");
    }

    @Test
    void restrictedManaCannotPayForNonartifactSpell() {
        addCreatureReady(player1, new OsgoodOperationDouble());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new OsgoodOperationDouble()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void restrictedManaPaysForArtifactAbility() {
        addCreatureReady(player1, new OsgoodOperationDouble());
        harness.addToBattlefield(player1, new SonicScrewdriver());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SonicScrewdriver());
        target.tap();
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void originalAndTokenEachInvestigateForAnOutsideHandSpell() {
        harness.setHand(player1, List.of(new OsgoodOperationDouble()));
        addOsgoodMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        SonicScrewdriver exiledSpell = new SonicScrewdriver();
        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, exiledSpell.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void opponentsOutsideHandSpellDoesNotInvestigate() {
        harness.addToBattlefield(player1, new OsgoodOperationDouble());
        SonicScrewdriver exiledSpell = new SonicScrewdriver();
        gd.addToExile(player2.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castFromExile(player2, exiledSpell.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateACopy() {
        harness.enterBattlefieldAndReturn(player1, new OsgoodOperationDouble());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Osgood, Operation Double")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void investigatedClueCanBeSacrificedToDrawUsingRestrictedMana() {
        addCreatureReady(player1, new OsgoodOperationDouble());
        SonicScrewdriver exiledSpell = new SonicScrewdriver();
        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, exiledSpell.getId());
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SonicScrewdriver()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);

        harness.activateAbility(player1, clueIndex, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInHand(player1, "Sonic Screwdriver");
    }

    private void addOsgoodMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}

package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WiccanYoungAvenger.class, Forest.class, Shock.class, GrizzlyBears.class})
class WiccanYoungAvengerTest extends BaseCardTest {

    @Test
    void noncreatureSpellExilesTopCardAndGrantsPlayPermission() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void creatureSpellDoesNotTriggerTheAbility() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void exiledLandCanBePlayedDuringMainPhase() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void playPermissionEndsWhenNextEndStepBegins() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void permissionGrantedDuringOwnEndStepSurvivesOpponentsEndStep() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        harness.assertLife(player2, 16);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerAbility() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
    }

    @Test
    void emptyLibraryDoesNotPreventTriggerOrSpellFromResolving() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void playingExiledSpellStillRequiresItsManaCost() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void opponentsExtraTurnDoesNotExpirePermissionBeforeControllersNextTurn() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));
        harness.addToBattlefield(player1, new WiccanYoungAvenger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        gd.queueExtraTurnFirst(player2.getId(), false);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        harness.assertLife(player2, 16);
    }
}

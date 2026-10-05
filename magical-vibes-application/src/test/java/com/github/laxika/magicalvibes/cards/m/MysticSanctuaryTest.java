package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.f.ForeverYoung;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticSanctuary.class, Island.class, Opt.class, RovingKeep.class, ForeverYoung.class})
class MysticSanctuaryTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanThreeOtherIslands() {
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        assertThat(findSanctuary(player1).isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void entersUntappedAndOffersAnInstantOrSorceryFromTheGraveyardWithThreeOtherIslands() {
        Card instant = new Opt();
        Card creature = new RovingKeep();
        harness.setGraveyard(player1, List.of(instant, creature));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        assertThat(findSanctuary(player1).isTapped()).isFalse();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(instant.getId());
        harness.assertNotInGraveyard(player1, "Opt");
    }

    @Test
    void triggerStillResolvesIfSanctuaryIsTappedAfterEnteringUntapped() {
        Card instant = new Opt();
        harness.setGraveyard(player1, List.of(instant));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        int sanctuaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findSanctuary(player1));
        harness.activateAbility(player1, sanctuaryIndex, 0, null, null);

        assertThat(findSanctuary(player1).isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(instant.getId());
    }

    @Test
    void tapsForBlueMana() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new MysticSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sanctuary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void recoversASorceryOnTopOfTheExistingLibrary() {
        Card sorcery = new ForeverYoung();
        Card libraryCard = new Island();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setLibrary(player1, List.of(libraryCard));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(sorcery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sorcery, libraryCard);
        harness.assertNotInGraveyard(player1, "Forever Young");
    }

    @Test
    void canDeclineRecoveryAfterChoosingTheTarget() {
        Card instant = new Opt();
        Card libraryCard = new Island();
        harness.setGraveyard(player1, List.of(instant));
        harness.setLibrary(player1, List.of(libraryCard));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyTargetsCardsInItsControllersGraveyard() {
        Card ownInstant = new Opt();
        Card opposingInstant = new Opt();
        harness.setGraveyard(player1, List.of(ownInstant));
        harness.setGraveyard(player2, List.of(opposingInstant));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownInstant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownInstant.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ownInstant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingInstant);
    }

    @Test
    void entersUntappedWithoutALegalGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new RovingKeep(), new Island()));
        harness.setGraveyard(player2, List.of(new Opt()));
        addIsland(player1);
        addIsland(player1);
        addIsland(player1);

        playSanctuary();

        assertThat(findSanctuary(player1).isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Roving Keep");
    }

    @Test
    void enteringTappedDoesNotTriggerEvenWithALegalGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new Opt()));
        addIsland(player1);
        addIsland(player1);
        addIsland(player2);
        addIsland(player2);
        addIsland(player2);

        playSanctuary();

        assertThat(findSanctuary(player1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Opt");
    }

    private void playSanctuary() {
        harness.setHand(player1, List.of(new MysticSanctuary()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void addIsland(Player player) {
        harness.addToBattlefield(player, new Island());
    }

    private Permanent findSanctuary(Player player) {
        return findPermanent(player, "Mystic Sanctuary");
    }
}

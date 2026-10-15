package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExpeditionChampion;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.e.ExpeditionSkulker;
import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
import com.github.laxika.magicalvibes.cards.p.ProwlingFelidar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThwartTheGrave.class, ExpeditionChampion.class, ExpeditionHealer.class, ExpeditionSkulker.class,
        ExpeditionDiviner.class, ProwlingFelidar.class, TajuruParagon.class})
class ThwartTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and an optional party creature simultaneously")
    void returnsCreatureAndPartyCreature() {
        Card creature = new ProwlingFelidar();
        Card partyCreature = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(creature, partyCreature));
        castThwartTheGrave(6);

        choose(creature);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(partyCreature.getId());
        choose(partyCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(creature.getId(), partyCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId())
                        || card.getId().equals(partyCreature.getId()));
    }

    @Test
    @DisplayName("The first target may be a non-party creature")
    void firstTargetMayBeNonPartyCreature() {
        Card creature = new ProwlingFelidar();
        harness.setGraveyard(player1, List.of(creature));
        castThwartTheGrave(6);

        choose(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("The same party creature may be chosen for both targets")
    void samePartyCreatureMayBeChosenForBothTargets() {
        Card partyCreature = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(partyCreature));
        castThwartTheGrave(6);

        choose(partyCreature);
        choose(partyCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(partyCreature.getId());
    }

    @Test
    @DisplayName("A full party reduces the generic cost by four")
    void fullPartyReducesGenericCostByFour() {
        addFullParty();
        Card creature = new ProwlingFelidar();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ThwartTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without a legal first creature target")
    void requiresFirstCreatureTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new ThwartTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The optional party target may be declined even when available")
    void mayDeclineAvailablePartyTarget() {
        Card creature = new ProwlingFelidar();
        Card cleric = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(creature, cleric));
        castThwartTheGrave(6);

        choose(creature);
        chooseNothing();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prowling Felidar");
        harness.assertNotOnBattlefield(player1, "Expedition Healer");
        harness.assertInGraveyard(player1, "Expedition Healer");
    }

    @Test
    @DisplayName("Only party creatures in your own graveyard qualify for the second target")
    void optionalTargetFiltersTypesAndGraveyardOwner() {
        Card creature = new ProwlingFelidar();
        Card otherCreature = new ProwlingFelidar();
        Card cleric = new ExpeditionHealer();
        Card rogue = new ExpeditionSkulker();
        Card warrior = new ExpeditionChampion();
        Card wizard = new ExpeditionDiviner();
        Card noncreature = new ThwartTheGrave();
        harness.setGraveyard(player1, List.of(creature, otherCreature, cleric, rogue, warrior, wizard, noncreature));
        harness.setGraveyard(player2, List.of(new ExpeditionHealer()));
        castThwartTheGrave(6);

        choose(creature);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                cleric.getId(), rogue.getId(), warrior.getId(), wizard.getId());
        choose(wizard);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prowling Felidar");
        harness.assertOnBattlefield(player1, "Expedition Diviner");
        harness.assertInGraveyard(player1, "Expedition Skulker");
        harness.assertInGraveyard(player2, "Expedition Healer");
    }

    @Test
    @DisplayName("The remaining legal party target returns when the first target leaves the graveyard")
    void returnsPartyTargetWhenFirstTargetBecomesIllegal() {
        Card creature = new ProwlingFelidar();
        Card cleric = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(creature, cleric));
        castThwartTheGrave(6);
        choose(creature);
        choose(cleric);

        harness.setGraveyard(player1, List.of(cleric));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Expedition Healer");
        harness.assertNotOnBattlefield(player1, "Prowling Felidar");
    }

    @Test
    @DisplayName("The first target returns when the optional target leaves the graveyard")
    void returnsFirstTargetWhenPartyTargetBecomesIllegal() {
        Card creature = new ProwlingFelidar();
        Card cleric = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(creature, cleric));
        castThwartTheGrave(6);
        choose(creature);
        choose(cleric);

        harness.setGraveyard(player1, List.of(creature));
        harness.setExile(player1, List.of(cleric));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prowling Felidar");
        harness.assertNotOnBattlefield(player1, "Expedition Healer");
    }

    @Test
    @DisplayName("Multiple Clerics count as one party member and opposing creatures do not count")
    void duplicateRolesAndOpponentsDoNotIncreaseReduction() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ProwlingFelidar());
        harness.addToBattlefield(player2, new ExpeditionSkulker());
        harness.addToBattlefield(player2, new ExpeditionChampion());
        harness.addToBattlefield(player2, new ExpeditionDiviner());
        Card creature = new ProwlingFelidar();
        harness.setGraveyard(player1, List.of(creature));
        castThwartTheGrave(6);
        choose(creature);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Prowling Felidar");
        harness.assertNotInGraveyard(player1, "Prowling Felidar");
    }

    @Test
    @DisplayName("A creature with all four party types still reduces the cost by only one")
    void multiRoleCreatureCountsOnlyOnce() {
        harness.addToBattlefield(player1, new TajuruParagon());
        Card creature = new ProwlingFelidar();
        harness.setGraveyard(player1, List.of(creature));
        castThwartTheGrave(6);
        choose(creature);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Prowling Felidar");
    }

    @Test
    @DisplayName("Party types granted in all zones qualify for the optional target")
    void grantedPartyTypesQualifyInGraveyard() {
        Card creature = new ProwlingFelidar();
        Card paragon = new TajuruParagon();
        harness.setGraveyard(player1, List.of(creature, paragon));
        castThwartTheGrave(6);
        choose(creature);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(paragon.getId());
        choose(paragon);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prowling Felidar");
        harness.assertOnBattlefield(player1, "Tajuru Paragon");
    }

    private void castThwartTheGrave(int mana) {
        harness.setHand(player1, List.of(new ThwartTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, mana);
        harness.castSorcery(player1, 0, 0);
    }

    private void choose(Card card) {
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }

    private void chooseNothing() {
        harness.handleMultipleCardsChosen(player1, List.of());
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ExpeditionSkulker());
        harness.addToBattlefield(player1, new ExpeditionChampion());
        harness.addToBattlefield(player1, new ExpeditionDiviner());
    }
}

package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.l.LovestruckBeast;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KessDissidentMage.class, Shock.class, GrizzlyBears.class, Divination.class, ThinkTwice.class,
        LovestruckBeast.class, HeartsDesire.class})
class KessDissidentMageTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an instant from the graveyard and exiles it")
    void castsInstantFromGraveyardAndExilesIt() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
    }

    @Test
    @DisplayName("Casts a sorcery from the graveyard and exiles it")
    void castsSorceryFromGraveyardAndExilesIt() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
    }

    @Test
    @DisplayName("Allows only one instant or sorcery graveyard cast each turn")
    void allowsOnlyOneCastEachTurn() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not cast permanent cards from the graveyard")
    void doesNotCastPermanentCards() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only allows the graveyard cast during the controller's turn")
    void onlyAllowsCastDuringControllerTurn() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kess can cast a card with flashback for its normal mana cost")
    void castsFlashbackCardForNormalManaCost() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        ThinkTwice spell = new ThinkTwice();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("A flashback cast does not consume Kess's permission")
    void flashbackDoesNotConsumeKessPermission() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Instants can be cast from the graveyard during upkeep")
    void castsInstantDuringUpkeep() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Kess does not allow a sorcery to be cast during upkeep")
    void rejectsSorceryDuringUpkeep() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kess does not waive the spell's mana cost")
    void requiresManaAndFailedAttemptDoesNotConsumePermission() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock()));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The spell is still exiled if Kess leaves before it resolves")
    void exilesSpellAfterKessLeaves() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        var kessId = harness.getPermanentId(player1, "Kess, Dissident Mage");
        Shock spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, kessId);
        harness.castAndResolveInstant(player1, 0, kessId);
        harness.assertNotOnBattlefield(player1, "Kess, Dissident Mage");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("The permission is spent when the spell is cast, before resolution")
    void permissionIsSpentWhileSpellIsOnStack() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The spell is exiled when its only target becomes illegal")
    void exilesSpellWithIllegalTarget() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Shock spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Kess can cast an Adventure sorcery from the graveyard")
    void castsAdventureFromGraveyard() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        LovestruckBeast beast = new LovestruckBeast();
        harness.setGraveyard(player1, List.of(beast, new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);

        harness.castAdventureFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isToken());
        assertThat(gd.findExiledCard(beast.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

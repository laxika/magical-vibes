package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WiccanRisingMagician.class, GrizzlyBears.class, Island.class, Spellbook.class})
class WiccanRisingMagicianTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell exiles another permanent until the next end step")
    void noncreatureSpellExilesAndReturnsAnotherPermanent() {
        harness.addToBattlefield(player1, new WiccanRisingMagician());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The trigger does not trigger for a creature spell")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new WiccanRisingMagician());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger cannot target Wiccan itself, a land, or a token")
    void rejectsInvalidTargets() {
        Permanent wiccan = harness.addToBattlefieldAndReturn(player1, new WiccanRisingMagician());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent token = harness.addToBattlefieldAndReturn(player2, token());
        Permanent validTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, wiccan.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, token.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, validTarget.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Wiccan")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new WiccanRisingMagician());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Wiccan can exile another noncreature permanent its controller controls")
    void canTargetOwnNoncreaturePermanent() {
        harness.addToBattlefield(player1, new WiccanRisingMagician());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Spellbook).hasSize(2);
    }

    @Test
    @DisplayName("A stolen permanent returns to its owner even after Wiccan leaves")
    void stolenPermanentReturnsToOwnerAfterSourceLeaves() {
        Permanent wiccan = harness.addToBattlefieldAndReturn(player1, new WiccanRisingMagician());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wiccan);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isNotEqualTo(target.getId());
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting without any legal target still resolves the noncreature spell")
    void noLegalTargetDoesNotBlockSpell() {
        harness.addToBattlefield(player1, new WiccanRisingMagician());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Wiccan, Rising Magician");
    }

    private static Card token() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}

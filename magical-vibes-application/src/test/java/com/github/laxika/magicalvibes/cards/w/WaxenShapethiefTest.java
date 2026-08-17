package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WaxenShapethiefTest extends BaseCardTest {

    private void castWaxenShapethief() {
        harness.setHand(player1, List.of(new WaxenShapethief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Waxen Shapethief enters as a copy of a creature its controller controls")
    void copiesOwnCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        castWaxenShapethief();

        harness.handleMayAbilityChosen(player1, true);

        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");
        harness.handlePermanentChosen(player1, elementalId);

        GameData gd = harness.getGameData();
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Waxen Shapethief"))
                .findFirst().orElse(null);

        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(copy.getCard().getPower()).isEqualTo(4);
        assertThat(copy.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Waxen Shapethief can copy a noncreature artifact its controller controls")
    void copiesOwnArtifact() {
        harness.addToBattlefield(player1, new JayemdaeTome());
        castWaxenShapethief();

        harness.handleMayAbilityChosen(player1, true);

        UUID tomeId = harness.getPermanentId(player1, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        GameData gd = harness.getGameData();
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Waxen Shapethief"))
                .findFirst().orElse(null);

        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Jayemdae Tome");
        assertThat(copy.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Waxen Shapethief cannot copy an artifact or creature an opponent controls")
    void cannotCopyOpponentPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.addToBattlefield(player1, new AirElemental());
        castWaxenShapethief();

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID tomeId = harness.getPermanentId(player2, "Jayemdae Tome");
        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(elementalId)
                .doesNotContain(bearsId, tomeId);
    }

    @Test
    @DisplayName("Cycling Waxen Shapethief discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new WaxenShapethief()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Waxen Shapethief");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}

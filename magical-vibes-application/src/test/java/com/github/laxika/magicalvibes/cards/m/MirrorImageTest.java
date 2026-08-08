package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorImageTest extends BaseCardTest {

    private void castMirrorImage() {
        harness.setHand(player1, List.of(new MirrorImage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
    }

    @Test
    @DisplayName("Mirror Image enters as a copy of a creature its controller controls")
    void copiesOwnCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        castMirrorImage();

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");
        harness.handlePermanentChosen(player1, elementalId);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Mirror Image"))
                .findFirst().orElse(null);

        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(copy.getCard().getPower()).isEqualTo(4);
        assertThat(copy.getCard().getToughness()).isEqualTo(4);
        assertThat(copy.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Mirror Image cannot copy a creature an opponent controls")
    void cannotCopyOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        castMirrorImage();

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(elementalId)
                .doesNotContain(bearsId);
    }

    @Test
    @DisplayName("Mirror Image enters as a 0/0 and dies when the controller declines to copy")
    void diesWhenDeclining() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castMirrorImage();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Mirror Image"));
        harness.assertInGraveyard(player1, "Mirror Image");
    }
}

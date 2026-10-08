package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BribersPurse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheDragonspeaker;
import com.github.laxika.magicalvibes.cards.s.SuspensionField;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UtterEnd.class, Forest.class, GrizzlyBears.class, BribersPurse.class, SuspensionField.class, SarkhanTheDragonspeaker.class})
class UtterEndTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target nonland permanent")
    void exilesTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        prepareUtterEnd();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new UtterEnd()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Fizzles when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        prepareUtterEnd();
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Exiles an artifact controlled by its caster")
    void exilesOwnArtifact() {
        harness.addToBattlefield(player1, new BribersPurse());
        UUID targetId = harness.getPermanentId(player1, "Briber's Purse");

        prepareUtterEnd();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Briber's Purse");
        harness.assertNotInGraveyard(player1, "Briber's Purse");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Briber's Purse"));
        harness.assertInGraveyard(player1, "Utter End");
    }

    @Test
    @DisplayName("Exiles a noncreature enchantment")
    void exilesNoncreatureEnchantment() {
        harness.addToBattlefield(player2, new SuspensionField());
        UUID targetId = harness.getPermanentId(player2, "Suspension Field");

        prepareUtterEnd();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Suspension Field");
        harness.assertNotInGraveyard(player2, "Suspension Field");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Suspension Field"));
    }

    @Test
    @DisplayName("Exiles a planeswalker")
    void exilesPlaneswalker() {
        harness.addToBattlefield(player2, new SarkhanTheDragonspeaker());
        UUID targetId = harness.getPermanentId(player2, "Sarkhan, the Dragonspeaker");

        prepareUtterEnd();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Sarkhan, the Dragonspeaker");
        harness.assertNotInGraveyard(player2, "Sarkhan, the Dragonspeaker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Sarkhan, the Dragonspeaker"));
    }

    private void prepareUtterEnd() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new UtterEnd()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

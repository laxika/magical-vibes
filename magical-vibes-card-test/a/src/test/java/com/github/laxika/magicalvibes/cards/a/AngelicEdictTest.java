package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Brushland;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicEdict.class, Brushland.class, FountainOfYouth.class, GloriousAnthem.class,
        GrizzlyBears.class, Unsummon.class})
class AngelicEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature")
    void exilesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelicEdict()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles a target enchantment")
    void exilesTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new AngelicEdict()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new AngelicEdict()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile its controller's creature without affecting other permanents")
    void exilesOwnCreatureOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new AngelicEdict()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Angelic Edict");
    }

    @Test
    @DisplayName("Cannot target a land that is neither a creature nor an enchantment")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Brushland());
        harness.setHand(player1, List.of(new AngelicEdict()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Brushland");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not exile a creature returned to hand in response")
    void doesNotExileTargetThatLeftBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelicEdict()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Angelic Edict");
        assertThat(gd.stack).isEmpty();
    }
}

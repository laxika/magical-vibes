package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScorchTheFieldsTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting targets a land and puts spell on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ScorchTheFields()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Scorch the Fields");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScorchTheFields()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ScorchTheFields()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Deals 1 damage to each Human creature on both sides")
    void deals1DamageToEachHumanCreature() {
        harness.addToBattlefield(player2, new Forest());
        addCreature(player1, createCreature("Human One", CardSubtype.HUMAN));
        addCreature(player2, createCreature("Human Two", CardSubtype.HUMAN));
        harness.setHand(player1, List.of(new ScorchTheFields()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Human One"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Human Two"));
    }

    @Test
    @DisplayName("Does not damage non-Human creatures but damages Human creatures")
    void doesNotDamageNonHumanCreatures() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCreature(player2, createCreature("Human", CardSubtype.HUMAN));
        harness.setHand(player1, List.of(new ScorchTheFields()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Human"));
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ScorchTheFields()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Fizzles if target land is removed — no damage dealt")
    void fizzlesIfTargetLandRemoved() {
        harness.addToBattlefield(player2, new Forest());
        addCreature(player2, createCreature("Human", CardSubtype.HUMAN));
        harness.setHand(player1, List.of(new ScorchTheFields()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Human"));
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TatsumasaTheDragonsFangTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +5/+5")
    void equippedCreatureGetsBoost() {
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        Permanent tatsumasa = addPermanent(player1, new TatsumasaTheDragonsFang());
        tatsumasa.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Activating the ability exiles Tatsumasa and creates a 5/5 flying Dragon Spirit token")
    void activationExilesSelfAndCreatesToken() {
        addPermanent(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tatsumasa, the Dragon's Fang");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));

        Permanent token = dragonSpirit();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Tatsumasa returns to the battlefield when the Dragon Spirit token dies")
    void tokenDeathReturnsTatsumasa() {
        addPermanent(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        killCreature(dragonSpirit());

        harness.assertOnBattlefield(player1, "Tatsumasa, the Dragon's Fang");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dragon Spirit"));
    }

    @Test
    @DisplayName("Tatsumasa stays in exile while the token is alive")
    void tatsumasaStaysExiledWhileTokenLives() {
        addPermanent(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tatsumasa, the Dragon's Fang");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));
    }

    @Test
    @DisplayName("The returned Tatsumasa can be activated again")
    void returnedTatsumasaCanBeActivatedAgain() {
        addPermanent(player1, new TatsumasaTheDragonsFang());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        killCreature(dragonSpirit());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tatsumasa, the Dragon's Fang"));
        assertThat(dragonSpirit()).isNotNull();
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent dragonSpirit() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dragon Spirit"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No Dragon Spirit token on the battlefield"));
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade — the token dies, its trigger goes on the stack
        harness.passBothPriorities(); // resolve the return trigger
    }
}

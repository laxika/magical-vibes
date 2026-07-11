package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeirdingShamanTest extends BaseCardTest {

    // ===== {3}{B}, Sacrifice a Goblin: Create two 1/1 black Goblin Rogue tokens =====

    @Test
    @DisplayName("Sacrificing a Goblin (itself) creates two 1/1 black Goblin Rogue tokens")
    void sacrificeGoblinCreatesTwoTokens() {
        addShamanReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        // Shaman is the only Goblin → auto-sacrifices itself to pay the cost
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countGoblinRogueTokens(player1)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Weirding Shaman"));
    }

    @Test
    @DisplayName("Goblin Rogue tokens are 1/1 black")
    void tokensAreOneOneBlack() {
        addShamanReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findGoblinRogueToken(player1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.GOBLIN, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("Ability requires {3}{B} to activate")
    void abilityRequiresMana() {
        addShamanReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addShamanReady(Player player) {
        Permanent shaman = new Permanent(new WeirdingShaman());
        shaman.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(shaman);
        return shaman;
    }

    private int countGoblinRogueTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Rogue"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOBLIN))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.ROGUE))
                .count();
    }

    private Permanent findGoblinRogueToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Rogue"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Goblin Rogue token found"));
    }
}

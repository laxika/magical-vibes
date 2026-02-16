package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeGolemTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Composite Golem has correct card properties")
    void hasCorrectProperties() {
        CompositeGolem card = new CompositeGolem();

        assertThat(card.getName()).isEqualTo("Composite Golem");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{6}");
        assertThat(card.getPower()).isEqualTo(4);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(6);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isEqualTo(new AwardManaEffect(ManaColor.WHITE));
        assertThat(ability.getEffects().get(2)).isEqualTo(new AwardManaEffect(ManaColor.BLUE));
        assertThat(ability.getEffects().get(3)).isEqualTo(new AwardManaEffect(ManaColor.BLACK));
        assertThat(ability.getEffects().get(4)).isEqualTo(new AwardManaEffect(ManaColor.RED));
        assertThat(ability.getEffects().get(5)).isEqualTo(new AwardManaEffect(ManaColor.GREEN));
    }

    // ===== Mana ability resolves immediately (CR 605.1a, CR 605.3a) =====

    @Test
    @DisplayName("Activating Composite Golem sacrifices it and adds WUBRG to mana pool immediately")
    void activateAbilityAddsWubrgImmediately() {
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // The permanent should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Composite Golem"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Composite Golem"));

        // Mana ability resolves immediately — no stack entry
        assertThat(gd.stack).isEmpty();

        // All five colors of mana should be in the pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Composite Golem can be activated with summoning sickness since it's a mana ability")
    void canActivateWithSummoningSickness() {
        // Mana abilities don't use the stack and don't require the creature to be free of summoning sickness
        // unless they require tapping (CR 605.1a). Composite Golem's ability does not require tapping.
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

        // Should succeed even though the creature just entered the battlefield
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Composite Golem"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Composite Golem mana can be used to cast spells")
    void manaCanBeUsedToCastSpells() {
        harness.addToBattlefield(player1, new CompositeGolem());

        GameData gd = harness.getGameData();

        // Activate to get WUBRG
        harness.activateAbility(player1, 0, null, null);

        // Verify 5 total mana
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }
}

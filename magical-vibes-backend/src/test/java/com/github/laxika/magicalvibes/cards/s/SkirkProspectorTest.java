package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SkirkProspectorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Skirk Prospector has sacrifice-goblin mana ability")
    void hasCorrectAbility() {
        SkirkProspector card = new SkirkProspector();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSubtypeCreatureCost.class);
        SacrificeSubtypeCreatureCost cost = (SacrificeSubtypeCreatureCost) ability.getEffects().get(0);
        assertThat(cost.subtype()).isEqualTo(CardSubtype.GOBLIN);
        assertThat(ability.getEffects().get(1)).isEqualTo(new AwardManaEffect(ManaColor.RED));
    }

    // ===== Mana ability behavior =====

    @Test
    @DisplayName("Sacrificing itself adds one red mana immediately (mana ability, no stack)")
    void sacrificeSelfAddsRedMana() {
        harness.addToBattlefield(player1, new SkirkProspector());

        harness.activateAbility(player1, 0, null, null);

        // Mana ability resolves immediately — no stack entry
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skirk Prospector"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Skirk Prospector"));
    }

    @Test
    @DisplayName("Can sacrifice another Goblin to add red mana")
    void sacrificeAnotherGoblinAddsRedMana() {
        harness.addToBattlefield(player1, new SkirkProspector());
        harness.addToBattlefield(player1, new SiegeGangCommander());

        // With multiple Goblins, activating should prompt for a choice
        harness.activateAbility(player1, 0, null, null);
        UUID siegeGangId = harness.getPermanentId(player1, "Siege-Gang Commander");
        harness.handlePermanentChosen(player1, siegeGangId);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Siege-Gang Commander"));
        // Skirk Prospector should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Skirk Prospector"));
    }

    @Test
    @DisplayName("Can activate with summoning sickness since it's a mana ability without tap")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SkirkProspector());

        // Should succeed even though the creature just entered the battlefield
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Goblin creature")
    void cannotSacrificeNonGoblin() {
        harness.addToBattlefield(player1, new SkirkProspector());
        // Add a non-Goblin creature (Siege-Gang Commander is a Goblin, so let's use Llanowar Elves)
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.l.LlanowarElves());

        // With only one Goblin (Skirk itself), it should auto-sacrifice itself
        harness.activateAbility(player1, 0, null, null);

        // Skirk should be sacrificed (auto-selected as the only Goblin)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skirk Prospector"));
        // Llanowar Elves should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}

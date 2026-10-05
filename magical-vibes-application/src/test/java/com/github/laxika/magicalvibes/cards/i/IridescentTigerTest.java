package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Aethersnatch;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IridescentTiger.class, BeaconOfUnrest.class, Aethersnatch.class})
class IridescentTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Iridescent Tiger adds one mana of each color")
    void castingItAddsOneManaOfEachColor() {
        harness.castFromHand(player1, new IridescentTiger(), "{4}{R}");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Putting Iridescent Tiger onto the battlefield without casting it adds no mana")
    void puttingItOntoTheBattlefieldWithoutCastingItAddsNoMana() {
        IridescentTiger target = new IridescentTiger();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Iridescent Tiger");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mana is added only after the enter ability resolves")
    void manaAbilityUsesTheStack() {
        harness.castFromHand(player1, new IridescentTiger(), "{4}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iridescent Tiger");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Gaining control of another player's Tiger spell does not trigger its mana ability")
    void stolenSpellDoesNotAddMana() {
        IridescentTiger tiger = new IridescentTiger();
        harness.castFromHand(player1, tiger, "{4}{R}");
        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tiger.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Iridescent Tiger");
        harness.assertNotOnBattlefield(player1, "Iridescent Tiger");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}

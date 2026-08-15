package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlpineMoonTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Alpine Moon offers only nonbasic land names")
    void choosesNonbasicLandName() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlpineMoon()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Glimmerpost").doesNotContain("Forest", "Grizzly Bears");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Forest"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid nonbasic land card name");

        harness.handleListChoice(player1, "Glimmerpost");

        assertThat(findPermanent(player1, "Alpine Moon").getChosenName()).isEqualTo("Glimmerpost");
    }

    @Test
    @DisplayName("Matching opponent lands lose their types and abilities but gain any-color mana")
    void changesMatchingOpponentLands() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Glimmerpost());
        opponentLand.setSummoningSick(false);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Glimmerpost());
        ownLand.setSummoningSick(false);

        Permanent alpineMoon = new Permanent(new AlpineMoon());
        alpineMoon.setChosenName("Glimmerpost");
        gd.playerBattlefields.get(player1.getId()).add(alpineMoon);

        var opponentBonus = gqs.computeStaticBonus(gd, opponentLand);
        assertThat(opponentBonus.losesAllAbilities()).isTrue();
        assertThat(opponentBonus.landSubtypeOverriding()).isTrue();
        assertThat(opponentBonus.grantedSubtypes()).isEmpty();
        assertThat(gqs.effectiveBasicLandTypes(gd, opponentLand)).isEmpty();
        assertThat(opponentBonus.grantedActivatedAbilities()).isNotEmpty();
        assertThat(gqs.isLand(gd, opponentLand)).isTrue();

        assertThat(gqs.computeStaticBonus(gd, ownLand).losesAllAbilities()).isFalse();

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(opponentLand.isTapped()).isTrue();
    }
}

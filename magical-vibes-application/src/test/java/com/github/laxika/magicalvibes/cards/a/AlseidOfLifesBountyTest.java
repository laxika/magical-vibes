package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlseidOfLifesBounty.class, GrizzlyBears.class, GloriousAnthem.class, Forest.class})
class AlseidOfLifesBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing this creature grants chosen-color protection to a creature you control")
    void sacrificeGrantsChosenColorProtectionToCreature() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        harness.assertInGraveyard(player1, "Alseid of Life's Bounty");
    }

    @Test
    @DisplayName("The ability can target an enchantment you control")
    void canTargetControlledEnchantment() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent target = findPermanent(player1, "Glorious Anthem");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's permanent or a land")
    void cannotTargetOpponentPermanentOrLand() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new Forest());
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                null,
                harness.getPermanentId(player1, "Forest")
        )).isInstanceOf(IllegalStateException.class);
    }
}

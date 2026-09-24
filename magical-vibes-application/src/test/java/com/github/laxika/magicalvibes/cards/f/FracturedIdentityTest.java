package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FracturedIdentity.class, GrizzlyBears.class, Forest.class})
class FracturedIdentityTest extends BaseCardTest {

    @Test
    void exilesTargetAndCreatesCopyForEachPlayerOtherThanItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFracturedIdentity(target, player1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    void doesNotTargetLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FracturedIdentity()));
        addFracturedIdentityMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFracturedIdentity(Permanent target, Player caster) {
        harness.setHand(caster, List.of(new FracturedIdentity()));
        addFracturedIdentityMana(caster);
        harness.castSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addFracturedIdentityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}

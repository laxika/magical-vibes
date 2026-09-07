package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SterlingKeykeeper.class, GrizzlyBears.class})
class SterlingKeykeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target non-Mount creature")
    void resolvingAbilityTapsTargetNonMountCreature() {
        addReadyKeykeeper(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a Mount creature")
    void cannotTargetMountCreature() {
        addReadyKeykeeper(player1);
        Permanent mount = addCreatureReady(player2, new GrizzlyBears());
        TestCards.mutableCard(mount).setSubtypes(List.of(CardSubtype.MOUNT));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mount.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not be a Mount");
    }

    @Test
    @DisplayName("Can target a creature controlled by the ability controller")
    void canTargetOwnCreature() {
        addReadyKeykeeper(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    private Permanent addReadyKeykeeper(Player player) {
        Permanent keykeeper = new Permanent(new SterlingKeykeeper());
        keykeeper.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(keykeeper);
        return keykeeper;
    }
}

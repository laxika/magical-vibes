package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ItsClobberinTime.class, HillGiant.class, GrizzlyBears.class,
        MindStone.class, GloriousAnthem.class})
class ItsClobberinTimeTest extends BaseCardTest {

    @Test
    @DisplayName("The clobbering mode deals the source creature's power to an opponent's creature")
    void dealsPowerDamageToOpponentsCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 1, new int[]{0},
                List.of(source.getId(), harness.getPermanentId(player2, "Grizzly Bears")), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The destruction mode destroys an artifact or enchantment")
    void destroysArtifactOrEnchantment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 1, new int[]{1},
                List.of(artifact.getId()), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("The destruction mode also destroys an enchantment")
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 1, new int[]{1},
                List.of(enchantment.getId()), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The clobbering mode requires a creature controlled by an opponent as its second target")
    void rejectsOwnCreatureAsSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ItsClobberinTime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 1,
                new int[]{0}, List.of(source.getId(), ownTarget.getId()), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("A resolved spell with rebound is exiled for its next upkeep")
    void resolvedSpellIsExiledForRebound() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        ItsClobberinTime card = new ItsClobberinTime();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 1, new int[]{1},
                List.of(artifact.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }
}

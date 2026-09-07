package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiningArmor.class, BenalishKnight.class, GrizzlyBears.class})
class ShiningArmorTest extends BaseCardTest {

    @Test
    void entersAttachedToTargetKnightYouControl() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        int initialToughness = gqs.getEffectiveToughness(gd, knight);
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0, knight.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent armor = findPermanent(player1, "Shining Armor");
        assertThat(armor.getAttachedTo()).isEqualTo(knight.getId());
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(initialToughness + 2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void canBeCastWithoutKnightAndEntersUnattached() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shining Armor").getAttachedTo()).isNull();
    }

    @Test
    void rejectsNonKnightEtbTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShiningArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Knight");
    }

    @Test
    void equipGrantsToughnessAndVigilanceToAnyCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int initialToughness = gqs.getEffectiveToughness(gd, creature);
        harness.addToBattlefield(player1, new ShiningArmor());
        harness.addMana(player1, ManaColor.WHITE, 3);

        int armorIndex = findPermanentIndex(player1, "Shining Armor");
        harness.activateAbility(player1, armorIndex, null, creature.getId());
        harness.passBothPriorities();

        Permanent armor = findPermanent(player1, "Shining Armor");
        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(initialToughness + 2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    private int findPermanentIndex(com.github.laxika.magicalvibes.model.Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }
}

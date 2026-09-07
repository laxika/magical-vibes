package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatterTheOath.class, GrizzlyBears.class, UnderworldDreams.class})
class ShatterTheOathTest extends BaseCardTest {

    @Test
    void destroysCreatureAndAttachesWickedRoleToYourCreature() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(List.of(destroyed.getId(), target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void destroysEnchantmentWhenRoleTargetIsOmitted() {
        harness.addToBattlefield(player2, new UnderworldDreams());
        Permanent enchantment = findPermanent(player2, "Underworld Dreams");
        cast(List.of(enchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Underworld Dreams");
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<UUID> targets) {
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CursedCourtier.class)
class CursedCourtierTest extends BaseCardTest {

    @Test
    void entersWithCursedRoleAttachedAndSetsItsBasePowerAndToughness() {
        harness.setHand(player1, List.of(new CursedCourtier()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent courtier = findPermanent(player1, "Cursed Courtier");
        Permanent role = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Cursed".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();

        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().isAura()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(courtier.getId());
        assertThat(gqs.getEffectivePower(gd, courtier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, courtier)).isEqualTo(1);
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VernalSovereign.class, GrizzlyBears.class})
class VernalSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a green and white Elemental whose P/T equals the creature count")
    void etbCreatesCreatureCountElemental() {
        castAndResolveSovereign();

        Permanent token = findElementalToken();
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elemental P/T updates as creatures enter and ignores opponent creatures")
    void tokenPowerToughnessTracksControlledCreatures() {
        castAndResolveSovereign();

        Permanent token = findElementalToken();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking creates a creature-count Elemental token")
    void attackCreatesCreatureCountElemental() {
        Permanent sovereign = new Permanent(new VernalSovereign());
        sovereign.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sovereign);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent token = findElementalToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    private void castAndResolveSovereign() {
        harness.setHand(player1, List.of(new VernalSovereign()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findElementalToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Elemental"))
                .findFirst()
                .orElseThrow();
    }
}

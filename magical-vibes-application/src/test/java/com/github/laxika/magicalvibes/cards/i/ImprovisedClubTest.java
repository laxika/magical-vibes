package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImprovisedClub.class, DarksteelRelic.class, GrizzlyBears.class, Forest.class})
class ImprovisedClubTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new ImprovisedClub()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Sacrificing a creature deals 4 damage to any target")
    void sacrificesCreatureAndDealsDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepare();

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Sacrificing an artifact deals 4 damage to a creature")
    void sacrificesArtifactAndDealsDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a nonartifact, noncreature permanent")
    void cannotSacrificeLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepare();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, player2.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

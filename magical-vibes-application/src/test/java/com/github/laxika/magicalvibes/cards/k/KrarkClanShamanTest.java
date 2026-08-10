package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KrarkClanShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact deals 1 damage to each creature without flying")
    void sacrificesArtifactAndDamagesNonFlyers() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new KrarkClanShaman());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, artifact());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Artifact");
        harness.assertNotOnBattlefield(player1, "Krark-Clan Shaman");
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Suntail Hawk").getMarkedDamage()).isZero();
        assertThat(shaman).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(artifact).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new KrarkClanShaman());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card artifact() {
        Card card = new Card();
        card.setName("Artifact");
        card.setType(CardType.ARTIFACT);
        return card;
    }
}

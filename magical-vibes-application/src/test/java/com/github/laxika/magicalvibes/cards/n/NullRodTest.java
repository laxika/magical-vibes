package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NullRodTest extends BaseCardTest {

    @Test
    void preventsArtifactManaAbilities() {
        addNullRod(player1);
        addArtifactWithManaAbility(player2);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void preventsNonManaArtifactAbilities() {
        addNullRod(player1);
        addArtifactWithActivatedAbility(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void doesNotPreventLandManaAbilities() {
        addNullRod(player1);

        Card land = new Card();
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(land));

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addNullRod(Player player) {
        return harness.addToBattlefieldAndReturn(player, new NullRod());
    }

    private void addArtifactWithManaAbility(Player player) {
        Card artifact = new Card();
        artifact.setName("Mana Artifact");
        artifact.setType(CardType.ARTIFACT);
        artifact.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        gd.playerBattlefields.get(player.getId()).add(new Permanent(artifact));
    }

    private void addArtifactWithActivatedAbility(Player player) {
        Card artifact = new Card();
        artifact.setName("Activated Artifact");
        artifact.setType(CardType.ARTIFACT);
        artifact.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        Permanent permanent = new Permanent(artifact);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}

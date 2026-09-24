package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelMutation.class, AirElemental.class, ProdigalPyromancer.class, Forest.class})
class DarksteelMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Turns the enchanted creature into an indestructible 0/1 artifact Insect")
    void mutatesEnchantedCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        attachMutation(target);

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.INSECT)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Removes activated abilities while leaving the Aura-granted indestructible ability")
    void removesOriginalActivatedAbilities() {
        Permanent target = addCreatureReady(player1, new ProdigalPyromancer());
        attachMutation(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");

        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Removing the Aura restores the creature")
    void removalRestoresCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        Permanent mutation = attachMutation(target);

        gd.playerBattlefields.get(player1.getId()).remove(mutation);

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.INSECT)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent attachMutation(Permanent target) {
        Permanent mutation = new Permanent(new DarksteelMutation());
        mutation.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(mutation);
        return mutation;
    }

}

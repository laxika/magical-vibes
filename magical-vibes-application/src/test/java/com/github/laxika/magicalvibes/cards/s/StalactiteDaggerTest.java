package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StalactiteDaggerTest extends BaseCardTest {

    @Test
    @DisplayName("When Stalactite Dagger enters, it creates a 1/1 colorless Shapeshifter with changeling")
    void createsShapeshifterTokenOnEnter() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StalactiteDagger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Shapeshifter")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == null
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getKeywords().contains(Keyword.CHANGELING));
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 and all creature types")
    void equippedCreatureGetsBoostAndAllCreatureTypes() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("Equip {2} attaches Stalactite Dagger to a creature you control")
    void equipAttachesToCreature() {
        Permanent dagger = addDaggerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addDaggerReady(Player player) {
        Permanent permanent = new Permanent(new StalactiteDagger());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

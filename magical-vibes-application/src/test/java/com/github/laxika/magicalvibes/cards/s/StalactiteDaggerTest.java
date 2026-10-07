package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LysAlanaInformant;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalactiteDagger.class, LysAlanaInformant.class, NamelessInversion.class})
class StalactiteDaggerTest extends BaseCardTest {

    @Test
    @DisplayName("When Stalactite Dagger enters, it creates a 1/1 colorless Shapeshifter with changeling")
    void createsShapeshifterTokenOnEnter() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StalactiteDagger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

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
        Permanent creature = addCreatureReady(player1, new LysAlanaInformant());
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new StalactiteDagger());
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.MERFOLK)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Stalactite Dagger to a creature you control")
    void equipAttachesToCreature() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new StalactiteDagger());
        Permanent creature = addCreatureReady(player1, new LysAlanaInformant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reEquippingMovesBoostAndCreatureTypes() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new StalactiteDagger());
        Permanent first = addCreatureReady(player1, new LysAlanaInformant());
        Permanent second = addCreatureReady(player1, new LysAlanaInformant());
        dagger.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.GOBLIN)).isTrue();
    }

    @Test
    void cannotEquipOpponentCreature() {
        harness.addToBattlefield(player1, new StalactiteDagger());
        Permanent creature = addCreatureReady(player2, new LysAlanaInformant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEquipWithOnlyOneMana() {
        harness.addToBattlefield(player1, new StalactiteDagger());
        Permanent creature = addCreatureReady(player1, new LysAlanaInformant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void cannotEquipDuringOpponentTurn() {
        harness.addToBattlefield(player1, new StalactiteDagger());
        Permanent creature = addCreatureReady(player1, new LysAlanaInformant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void laterCreatureTypeRemovalDoesNotLeaveSharedTypes() {
        Permanent creature = addCreatureReady(player1, new LysAlanaInformant());
        Permanent other = addCreatureReady(player1, new LysAlanaInformant());
        for (int i = 0; i < 3; i++) {
            Permanent dagger = harness.addToBattlefieldAndReturn(player1, new StalactiteDagger());
            dagger.setAttachedTo(creature.getId());
        }
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ELF)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.shareCreatureType(gd, creature, other)).isFalse();
    }
}

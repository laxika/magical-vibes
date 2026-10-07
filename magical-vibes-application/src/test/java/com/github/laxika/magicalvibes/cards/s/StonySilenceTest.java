package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.a.AvacynianPriest;
import com.github.laxika.magicalvibes.cards.b.BlazingTorch;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GeistcatchersRig;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonySilence.class, SolRing.class, TravelersAmulet.class, Forest.class,
        AvacynianPriest.class, ManorGargoyle.class, SilverInlaidDagger.class, BlazingTorch.class,
        SongOfTheDryads.class, GeistcatchersRig.class, AbbeyGriffin.class})
class StonySilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks mana abilities of artifacts")
    void blocksManaAbilitiesOfArtifacts() {
        harness.addToBattlefield(player1, new StonySilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Blocks mana abilities of own artifacts")
    void blocksManaAbilitiesOfOwnArtifacts() {
        harness.addToBattlefield(player1, new StonySilence());
        harness.addToBattlefield(player1, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
    }

    @Test
    @DisplayName("Blocks non-mana activated abilities of artifacts before costs are paid")
    void blocksActivatedAbilitiesOfArtifacts() {
        harness.addToBattlefield(player1, new StonySilence());
        harness.addToBattlefield(player2, new TravelersAmulet());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
        harness.assertOnBattlefield(player2, "Traveler's Amulet");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not block mana abilities of non-artifact lands")
    void doesNotBlockLandManaAbilities() {
        harness.addToBattlefield(player1, new StonySilence());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not block activated abilities of non-artifact creatures")
    void doesNotBlockCreatureActivatedAbilities() {
        harness.addToBattlefield(player1, new StonySilence());
        addCreatureReady(player2, new AvacynianPriest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ManorGargoyle());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blocks activated abilities of artifact creatures")
    void blocksArtifactCreatureAbilities() {
        harness.addToBattlefield(player1, new StonySilence());
        addCreatureReady(player2, new ManorGargoyle());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
    }

    @Test
    @DisplayName("Removing Stony Silence re-enables artifact abilities")
    void removingStonySilenceReenablesAbilities() {
        Permanent silence = harness.addToBattlefieldAndReturn(player1, new StonySilence());
        harness.addToBattlefield(player2, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(silence);
        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing one Stony Silence leaves the other restriction active")
    void multipleStonysilencesStillBlock() {
        Permanent silence = harness.addToBattlefieldAndReturn(player1, new StonySilence());
        harness.addToBattlefield(player2, new StonySilence());
        harness.addToBattlefield(player2, new SolRing());

        gd.playerBattlefields.get(player1.getId()).remove(silence);

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Resolving Stony Silence immediately blocks artifacts already on the battlefield")
    void resolvingStonySilenceBlocksExistingArtifacts() {
        harness.addToBattlefield(player2, new SolRing());
        harness.setHand(player1, List.of(new StonySilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stony Silence");
    }

    @Test
    @DisplayName("Blocks equip but preserves the static bonus of attached equipment")
    void blocksEquipWithoutRemovingStaticBonus() {
        harness.addToBattlefield(player1, new StonySilence());
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new SilverInlaidDagger());
        Permanent creature = addCreatureReady(player1, new AvacynianPriest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stony Silence");
        assertThat(dagger.getAttachedTo()).isNull();

        int powerBefore = gqs.getEffectivePower(gd, creature);
        dagger.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(powerBefore + 3);
    }

    @Test
    @DisplayName("Does not block an ability granted by an artifact to a non-artifact creature")
    void doesNotBlockAbilityGrantedByBlazingTorch() {
        harness.addToBattlefield(player1, new StonySilence());
        Permanent creature = addCreatureReady(player1, new AvacynianPriest());
        Permanent torch = harness.addToBattlefieldAndReturn(player1, new BlazingTorch());
        torch.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Blazing Torch");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not stop an artifact ability activated before Stony Silence resolves")
    void doesNotCounterAlreadyActivatedAbility() {
        harness.addToBattlefield(player1, new SilverInlaidDagger());
        Permanent creature = addCreatureReady(player1, new AvacynianPriest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.addToBattlefield(player2, new StonySilence());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Does not block triggered abilities of artifacts")
    void doesNotBlockArtifactTriggeredAbilities() {
        harness.addToBattlefield(player1, new StonySilence());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());

        harness.castFromHand(player1, new GeistcatchersRig(), "{6}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, griffin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Abbey Griffin");
        harness.assertOnBattlefield(player1, "Geistcatcher's Rig");
    }

    @Test
    @DisplayName("Turning Stony Silence into a Forest re-enables artifact mana abilities")
    void losingPrintedAbilityReenablesArtifactManaAbilities() {
        Permanent silence = harness.addToBattlefieldAndReturn(player1, new StonySilence());
        Permanent song = harness.addToBattlefieldAndReturn(player2, new SongOfTheDryads());
        song.setAttachedTo(silence.getId());
        harness.addToBattlefield(player2, new SolRing());

        harness.activateAbility(player2, 1, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gqs.canActivateManaAbility(gd, gd.playerBattlefields.get(player2.getId()).get(1))).isTrue();
    }

    @Test
    @DisplayName("Turning Stony Silence into a Forest re-enables non-mana artifact abilities")
    void losingPrintedAbilityReenablesArtifactActivatedAbilities() {
        Permanent silence = harness.addToBattlefieldAndReturn(player1, new StonySilence());
        Permanent song = harness.addToBattlefieldAndReturn(player2, new SongOfTheDryads());
        song.setAttachedTo(silence.getId());
        harness.addToBattlefield(player2, new ManorGargoyle());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}

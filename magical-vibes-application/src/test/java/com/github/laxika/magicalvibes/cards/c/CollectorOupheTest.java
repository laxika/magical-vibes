package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectorOuphe.class, LightningGreaves.class, SongOfTheDryads.class, ProdigalSorcerer.class, SolRing.class, Forest.class})
class CollectorOupheTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks activated abilities of artifacts")
    void blocksArtifactAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        harness.addToBattlefield(player2, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Collector Ouphe");
    }

    @Test
    @DisplayName("Does not block activated abilities of non-artifact permanents")
    void doesNotBlockNonArtifactAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Collector Ouphe re-enables artifact abilities")
    void removingCollectorOupheReenablesArtifactAbilities() {
        Permanent ouphe = harness.addToBattlefieldAndReturn(player1, new CollectorOuphe());
        harness.addToBattlefield(player2, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(ouphe);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
    @Test
    @DisplayName("Blocks its controller's artifact mana abilities too")
    void blocksOwnArtifactManaAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        assertThat(ring.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Blocks equip, a non-mana activated ability")
    void blocksEquip() {
        Permanent ouphe = harness.addToBattlefieldAndReturn(player1, new CollectorOuphe());
        Permanent greaves = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, ouphe.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        assertThat(greaves.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-artifact creatures can still activate non-mana abilities")
    void permitsNonArtifactCreatureAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("An artifact ability already on the stack still resolves")
    void doesNotStopAlreadyActivatedAbility() {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        Permanent greaves = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        harness.activateAbility(player1, 1, 0, null, sorcerer.getId());

        harness.addToBattlefield(player2, new CollectorOuphe());
        harness.passBothPriorities();

        assertThat(greaves.getAttachedTo()).isEqualTo(sorcerer.getId());
    }

    @Test
    @DisplayName("Turning Collector Ouphe into a Forest removes its artifact ability lock")
    void losingPrintedAbilityReenablesArtifacts() {
        Permanent ouphe = harness.addToBattlefieldAndReturn(player1, new CollectorOuphe());
        harness.addToBattlefield(player1, new SolRing());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, ouphe.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("A former artifact that is now only a Forest can tap for mana")
    void usesCurrentArtifactType() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, ring.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}

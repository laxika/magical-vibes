package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArtfulTakedown;
import com.github.laxika.magicalvibes.cards.b.BargingSergeant;
import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.m.ManiacalRage;
import com.github.laxika.magicalvibes.cards.s.SelesnyaLocket;
import com.github.laxika.magicalvibes.cards.w.WishcoinCrab;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollarTheCulprit.class, ArtfulTakedown.class, BargingSergeant.class,
        HitchclawRecluse.class, ManiacalRage.class, SelesnyaLocket.class, WishcoinCrab.class})
class CollarTheCulpritTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with toughness 4 or greater")
    void destroysToughCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        prepareCollar();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature with toughness less than 4")
    void cannotTargetLowToughnessCreature() {
        harness.addToBattlefield(player2, new BargingSergeant());
        prepareCollar();

        UUID targetId = harness.getPermanentId(player2, "Barging Sergeant");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 4 or greater");
    }

    @Test
    @DisplayName("Destroys a creature with exactly four toughness")
    void destroysCreatureAtThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HitchclawRecluse());
        prepareCollar();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Hitchclaw Recluse");
        harness.assertInGraveyard(player2, "Hitchclaw Recluse");
    }

    @Test
    @DisplayName("Rejects a creature with exactly three toughness despite its high power")
    void rejectsCreatureJustBelowThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BargingSergeant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareCollar();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 4 or greater");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HitchclawRecluse());
        prepareCollar();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Hitchclaw Recluse");
        harness.assertInGraveyard(player1, "Hitchclaw Recluse");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SelesnyaLocket());
        prepareCollar();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses toughness granted by an Aura when checking the target")
    void destroysCreatureWithBoostedToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BargingSergeant());
        harness.setHand(player1, List.of(new ManiacalRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        prepareCollar();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Barging Sergeant");
        harness.assertInGraveyard(player2, "Barging Sergeant");
    }

    @Test
    @DisplayName("Does not destroy a target whose toughness drops below four before resolution")
    void rechecksToughnessOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WishcoinCrab());
        prepareCollar();
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new ArtfulTakedown()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player2, 0, 1, 2,
                new int[]{1}, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wishcoin Crab");
        harness.assertNotInGraveyard(player2, "Wishcoin Crab");
        harness.assertInGraveyard(player1, "Collar the Culprit");
    }

    private void prepareCollar() {
        harness.setHand(player1, List.of(new CollarTheCulprit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}

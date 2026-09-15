package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gloomhunter;
import com.github.laxika.magicalvibes.cards.h.HyenaUmbra;
import com.github.laxika.magicalvibes.cards.l.LagacLizard;
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

@CardUsed({Vendetta.class, LagacLizard.class, Gloomhunter.class, HyenaUmbra.class, Forest.class})
class VendettaTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature and its controller loses life equal to its toughness")
    void destroysCreatureAndControllerLosesLifeEqualToToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        target.setToughnessModifier(2);

        castVendetta(target);

        harness.assertNotOnBattlefield(player2, "Lagac Lizard");
        harness.assertInGraveyard(player2, "Lagac Lizard");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        target.setRegenerationShield(1);

        castVendetta(target);

        harness.assertNotOnBattlefield(player2, "Lagac Lizard");
        harness.assertInGraveyard(player2, "Lagac Lizard");
    }

    @Test
    @DisplayName("Still causes life loss when the target is indestructible")
    void causesLifeLossWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castVendetta(target);

        harness.assertOnBattlefield(player2, "Lagac Lizard");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses toughness after umbra armor replaces the destruction")
    void usesToughnessAfterUmbraArmorReplacesDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HyenaUmbra());
        aura.setAttachedTo(target.getId());

        castVendetta(target);

        harness.assertOnBattlefield(player2, "Lagac Lizard");
        harness.assertNotOnBattlefield(player2, "Hyena Umbra");
        harness.assertInGraveyard(player2, "Hyena Umbra");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gloomhunter());
        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Does not cause life loss when the target is illegal on resolution")
    void doesNotCauseLifeLossWhenTargetIsIllegalOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LagacLizard());
        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void castVendetta(Permanent target) {
        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}

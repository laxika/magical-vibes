package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Asphyxiate.class, SwordwiseCentaur.class, SpringleafDrum.class})
class AsphyxiateTest extends BaseCardTest {

    private void prepareSpell() {
        harness.setHand(player1, List.of(new Asphyxiate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys target untapped creature")
    void destroysUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a tapped creature")
    void cannotTargetTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        target.tap();
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target becomes tapped before resolution")
    void fizzlesIfTargetBecomesTappedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        target.tap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can destroy an untapped creature controlled by the caster")
    void destroysOwnUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target an untapped noncreature permanent")
    void cannotTargetUntappedNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpringleafDrum());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a target that becomes tapped then untapped before resolution")
    void destroysTargetUntappedAgainBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordwiseCentaur());
        prepareSpell();

        harness.castSorcery(player1, 0, target.getId());
        target.tap();
        target.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.n.NyxbornShieldmate;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Excoriate.class, NyxbornShieldmate.class, SpringleafDrum.class})
class ExcoriateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target tapped creature")
    void exilesTargetTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornShieldmate());
        target.tap();

        harness.setHand(player1, java.util.List.of(new Excoriate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornShieldmate());

        harness.setHand(player1, java.util.List.of(new Excoriate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature artifact")
    void cannotTargetTappedNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpringleafDrum());
        target.tap();

        harness.setHand(player1, java.util.List.of(new Excoriate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Does not exile a creature that untaps before resolution")
    void doesNotExileTargetThatUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornShieldmate());
        target.tap();

        harness.setHand(player1, java.util.List.of(new Excoriate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());

        target.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player1, "Excoriate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can exile a tapped creature controlled by its caster")
    void exilesOwnTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornShieldmate());
        target.tap();

        harness.setHand(player1, java.util.List.of(new Excoriate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotInGraveyard(player1, "Nyxborn Shieldmate");
    }
}

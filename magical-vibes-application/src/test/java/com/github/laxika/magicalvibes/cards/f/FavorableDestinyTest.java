package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Brushwagg;
import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
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

@CardUsed({FavorableDestiny.class, EkunduGriffin.class, Brushwagg.class})
class FavorableDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("White enchanted creature gets +1/+2")
    void whiteCreatureGetsBoost() {
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin()); // 2/2 white
        attach(player1, griffin);

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Nonwhite enchanted creature gets no boost")
    void nonWhiteCreatureGetsNoBoost() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg()); // 3/2 green
        attach(player1, brushwagg);

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(2);
    }

    @Test
    @DisplayName("No shroud when the enchanted creature is the only creature its controller has")
    void noShroudWithoutAnotherCreature() {
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin());
        attach(player1, griffin);

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud while the enchanted creature's controller controls another creature")
    void shroudWithAnotherCreature() {
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin());
        attach(player1, griffin);
        addCreatureReady(player1, new Brushwagg());

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Another creature controlled by a different player does not grant shroud")
    void otherPlayersCreatureDoesNotGrantShroud() {
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin());
        attach(player1, griffin);
        addCreatureReady(player2, new Brushwagg());

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud follows the enchanted creature's controller, not the Aura's controller")
    void shroudReadsEnchantedCreaturesController() {
        Permanent griffin = addCreatureReady(player2, new EkunduGriffin());
        attach(player1, griffin);
        addCreatureReady(player1, new Brushwagg());

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isFalse();

        addCreatureReady(player2, new Brushwagg());

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud wears off when the other creature leaves the battlefield")
    void shroudWearsOffWhenAnotherCreatureLeaves() {
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin());
        attach(player1, griffin);
        Permanent other = addCreatureReady(player1, new Brushwagg());

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Removing Favorable Destiny removes both continuous effects")
    void effectsDisappearWhenAuraLeaves() {
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin());
        addCreatureReady(player1, new Brushwagg());
        Permanent aura = attach(player1, griffin);

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, griffin, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Resolving Favorable Destiny attaches it to a target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent griffin = addCreatureReady(player2, new EkunduGriffin());
        harness.setHand(player1, List.of(new FavorableDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, griffin.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Favorable Destiny")
                        && p.isAttached()
                        && p.getAttachedTo().equals(griffin.getId()));
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Favorable Destiny cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new FavorableDestiny());
        harness.setHand(player1, List.of(new FavorableDestiny()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attach(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FavorableDestiny());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoldarenBloodcasterTest extends BaseCardTest {

    @Test
    @DisplayName("Ally nontoken creature death creates a Blood token")
    void allyNontokenDeathCreatesBlood() {
        harness.addToBattlefield(player1, new VoldarenBloodcaster());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Kill only the bear so Voldaren stays on the battlefield to watch
        bear.setMarkedDamage(2);
        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(bloodTokenCount(player1)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Voldaren Bloodcaster"));
    }

    @Test
    @DisplayName("Own death creates a Blood token")
    void selfDeathCreatesBlood() {
        harness.addToBattlefield(player1, new VoldarenBloodcaster());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(bloodTokenCount(player1)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Voldaren Bloodcaster"));
    }

    @Test
    @DisplayName("Token creature death does not create a Blood token")
    void tokenCreatureDeathDoesNotCreateBlood() {
        harness.addToBattlefield(player1, new VoldarenBloodcaster());
        Card token = new Card();
        token.setName("Saproling");
        token.setType(CardType.CREATURE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        Permanent sap = harness.addToBattlefieldAndReturn(player1, token);

        sap.setMarkedDamage(1);
        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(bloodTokenCount(player1)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Voldaren Bloodcaster"));
    }

    @Test
    @DisplayName("Creating a fifth Blood token transforms into Bloodbat Summoner")
    void fifthBloodTransforms() {
        Permanent caster = harness.addToBattlefieldAndReturn(player1, new VoldarenBloodcaster());
        addBloodToken(player1);
        addBloodToken(player1);
        addBloodToken(player1);
        addBloodToken(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bear.setMarkedDamage(2);
        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(caster.isTransformed()).isTrue();
        assertThat(caster.getCard().getName()).isEqualTo("Bloodbat Summoner");
        assertThat(bloodTokenCount(player1)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform with fewer than five Blood tokens")
    void noTransformBelowFive() {
        Permanent caster = harness.addToBattlefieldAndReturn(player1, new VoldarenBloodcaster());
        addBloodToken(player1);
        addBloodToken(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bear.setMarkedDamage(2);
        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(caster.isTransformed()).isFalse();
        assertThat(bloodTokenCount(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Back face animates a Blood token into a 2/2 black Bat with flying and haste")
    void backFaceAnimatesBloodIntoBat() {
        Permanent caster = transformCaster();

        Permanent blood = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .findFirst().orElseThrow();

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, blood.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blood)).isTrue();
        assertThat(gqs.getEffectivePower(gd, blood)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blood)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blood, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, blood, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasColor(gd, blood, CardColor.BLACK)).isTrue();
        assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        assertThat(blood.getGrantedSubtypes()).contains(CardSubtype.BAT);
        assertThat(caster.getCard().getName()).isEqualTo("Bloodbat Summoner");
    }

    @Test
    @DisplayName("Back face combat trigger can decline targeting")
    void backFaceCanDeclineTarget() {
        transformCaster();

        Permanent blood = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .findFirst().orElseThrow();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blood)).isFalse();
    }

    private Permanent transformCaster() {
        Permanent caster = harness.addToBattlefieldAndReturn(player1, new VoldarenBloodcaster());
        addBloodToken(player1);
        addBloodToken(player1);
        addBloodToken(player1);
        addBloodToken(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setMarkedDamage(2);
        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(caster.isTransformed()).isTrue();
        return caster;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private long bloodTokenCount(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .count();
    }

    private void addBloodToken(Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setColor(null);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));
        bloodCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Discard a card, Sacrifice this token: Draw a card."
        ));
        Permanent blood = new Permanent(bloodCard);
        blood.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blood);
    }
}

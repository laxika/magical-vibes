package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChampionOfWits;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StripedRiverwinder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverwhelmingSplendor.class, AirElemental.class, JayemdaeTome.class, Plains.class,
        ChampionOfWits.class, NicolBolasGodPharaoh.class, StripedRiverwinder.class})
class OverwhelmingSplendorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Overwhelming Splendor attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new OverwhelmingSplendor()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Overwhelming Splendor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Enchanted player's creatures become base 1/1 and lose all abilities")
    void enchantedPlayerCreaturesAreNeutered() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        placeCurseOnPlayer(player1, player2);

        // 4/4 flyer -> base 1/1 with no flying
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Curse controller's own creatures are unaffected")
    void controllerCreaturesUnaffected() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Neuter effect wears off when the curse leaves the battlefield")
    void neuterRemovedWhenCurseLeaves() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent curse = placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(curse);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted player can't activate a non-mana activated ability")
    void enchantedPlayerCantActivateNonManaAbility() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        placeCurseOnPlayer(player1, player2);
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Overwhelming Splendor");
    }

    @Test
    @DisplayName("Enchanted player can still tap a land for mana")
    void enchantedPlayerCanStillTapForMana() {
        harness.addToBattlefield(player2, new Plains());
        placeCurseOnPlayer(player1, player2);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A player who is not enchanted can activate their abilities normally")
    void nonEnchantedPlayerUnaffected() {
        // Curse enchants player2, but its controller player1 is not restricted.
        harness.addToBattlefield(player1, new JayemdaeTome());
        placeCurseOnPlayer(player1, player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted player can activate loyalty abilities of a noncreature planeswalker")
    void enchantedPlayerCanActivateLoyaltyAbility() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);
        placeCurseOnPlayer(player1, player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enchanted player cannot cycle a card from hand")
    void enchantedPlayerCannotCycle() {
        StripedRiverwinder riverwinder = new StripedRiverwinder();
        harness.setHand(player2, List.of(riverwinder));
        harness.addMana(player2, ManaColor.BLUE, 1);
        placeCurseOnPlayer(player1, player2);

        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Overwhelming Splendor");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(riverwinder);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enchanted player cannot eternalize a card from the graveyard")
    void enchantedPlayerCannotEternalize() {
        ChampionOfWits champion = new ChampionOfWits();
        harness.setGraveyard(player2, List.of(champion));
        harness.addMana(player2, ManaColor.BLUE, 7);
        placeCurseOnPlayer(player1, player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Overwhelming Splendor");

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(champion);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after the curse lose their enters abilities")
    void enteringCreatureDoesNotTrigger() {
        placeCurseOnPlayer(player1, player2);

        Permanent champion = harness.enterBattlefieldAndReturn(player2, new ChampionOfWits());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Counters modify the creature's 1/1 base power and toughness")
    void countersStillModifyPowerAndToughness() {
        Permanent riverwinder = harness.addToBattlefieldAndReturn(player2, new StripedRiverwinder());
        riverwinder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, riverwinder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, riverwinder)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, riverwinder, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Non-mana activation restriction ends when the curse leaves")
    void activationRestrictionEndsWhenCurseLeaves() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        Permanent curse = placeCurseOnPlayer(player1, player2);
        harness.addMana(player2, ManaColor.WHITE, 4);
        gd.playerBattlefields.get(player1.getId()).remove(curse);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new OverwhelmingSplendor());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }
}

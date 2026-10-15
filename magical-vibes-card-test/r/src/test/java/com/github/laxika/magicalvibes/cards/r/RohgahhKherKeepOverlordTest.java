package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RohgahhKherKeepOverlord.class, KoboldsOfKherKeep.class, GrizzlyBears.class, DragonEgg.class})
class RohgahhKherKeepOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kobolds you control get +2/+2")
    void boostsOtherOwnKoboldsOnly() {
        Permanent overlord = addCreatureReady(player1, new RohgahhKherKeepOverlord());
        Permanent ownKobold = addCreatureReady(player1, new KoboldsOfKherKeep());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingKobold = addCreatureReady(player2, new KoboldsOfKherKeep());

        assertThat(gqs.getEffectivePower(gd, overlord)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, overlord)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownKobold)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownKobold)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingKobold)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposingKobold)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a Kobold and paying {2} creates a 4/4 flying Dragon")
    void payingForKoboldTriggerCreatesDragon() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.setHand(player1, List.of(new KoboldsOfKherKeep()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, dragon)).containsExactly(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a Dragon creates a named 0/1 red Kobold")
    void dragonSpellCreatesKobold() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent kobold = findPermanent(player1, "Kobolds of Kher Keep");
        assertThat(kobold.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, kobold)).containsExactly(CardColor.RED);
        assertThat(kobold.getCard().getSubtypes()).containsExactly(CardSubtype.KOBOLD);
    }

    @Test
    @DisplayName("Declining the Kobold trigger creates no Dragon")
    void decliningKoboldTriggerCreatesNoDragon() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.setHand(player1, List.of(new KoboldsOfKherKeep()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    @DisplayName("The Dragon is created before the Kobold spell resolves and does not trigger a Kobold token")
    void paidTriggerResolvesBeforeSpellWithoutTokenRecursion() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.setHand(player1, List.of(new KoboldsOfKherKeep()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(1);
        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Kobold spell can resolve without paying for a Dragon")
    void koboldSpellWithoutAvailableManaCreatesNoDragon() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.setHand(player1, List.of(new KoboldsOfKherKeep()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        harness.assertOnBattlefield(player1, "Kobolds of Kher Keep");
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Kobold spell does not trigger Rohgahh")
    void opposingKoboldSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new KoboldsOfKherKeep()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Kobolds of Kher Keep");
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
        assertThat(findPermanents(player2, "Dragon")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Dragon spell does not create a Kobold")
    void opposingDragonSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DragonEgg()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Dragon Egg");
        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).isEmpty();
        assertThat(findPermanents(player2, "Kobolds of Kher Keep")).isEmpty();
    }

    @Test
    @DisplayName("Putting Kobolds and Dragons directly onto the battlefield does not trigger Rohgahh")
    void directBattlefieldEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new RohgahhKherKeepOverlord());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player1, new KoboldsOfKherKeep());
        harness.addToBattlefield(player1, new DragonEgg());

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(1);
    }

    @Test
    @DisplayName("Casting Rohgahh itself does not trigger its own Kobold cast ability")
    void castingOverlordDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new RohgahhKherKeepOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Rohgahh, Kher Keep Overlord");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }
}

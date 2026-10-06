package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AcidSpewerDragon;
import com.github.laxika.magicalvibes.cards.d.DragonHunter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RapaciousDragon;
import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilumgarsScorn.class, GrizzlyBears.class, RapaciousDragon.class,
        DragonHunter.class, AcidSpewerDragon.class, Reverberate.class})
class SilumgarsScornTest extends BaseCardTest {

    @Test
    void countersSpellWhenDragonIsRevealed() {
        GrizzlyBears bears = new GrizzlyBears();
        RapaciousDragon dragon = new RapaciousDragon();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new SilumgarsScorn(), dragon));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithDiscard(player2, 0, bears.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(dragon);
    }

    @Test
    void countersSpellWhenDragonWasControlledAsCast() {
        harness.addToBattlefield(player2, new RapaciousDragon());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void targetSpellCanSurviveWhenControllerPaysWithoutDragonBonus() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetANonspell() {
        harness.setHand(player2, List.of(new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class})
    void countersSpellWhenControllerCannotPay() {
        DragonHunter hunter = new DragonHunter();
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, hunter.getId());

        harness.assertInGraveyard(player1, "Dragon Hunter");
        harness.assertNotOnBattlefield(player1, "Dragon Hunter");
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class})
    void countersSpellWhenControllerDeclinesPayment() {
        DragonHunter hunter = new DragonHunter();
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, hunter.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Dragon Hunter");
        harness.assertNotOnBattlefield(player1, "Dragon Hunter");
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class, AcidSpewerDragon.class})
    void unrevealedDragonInHandAndOpponentsDragonDoNotGrantBonus() {
        DragonHunter hunter = new DragonHunter();
        harness.addToBattlefield(player1, new AcidSpewerDragon());
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new SilumgarsScorn(), new AcidSpewerDragon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, hunter.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragon Hunter");
        harness.assertInHand(player2, "Acid-Spewer Dragon");
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class, AcidSpewerDragon.class})
    void revealingDragonPreventsPaymentEvenWhenControllerHasMana() {
        DragonHunter hunter = new DragonHunter();
        AcidSpewerDragon dragon = new AcidSpewerDragon();
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(dragon, new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstantWithDiscard(player2, 1, hunter.getId(), 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragon Hunter");
        assertThat(gd.playerHands.get(player2.getId())).contains(dragon);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class, AcidSpewerDragon.class})
    void dragonControlledAsCastStillGrantsBonusAfterLeavingBattlefield() {
        DragonHunter hunter = new DragonHunter();
        harness.addToBattlefield(player2, new AcidSpewerDragon());
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, hunter.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragon Hunter");
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class, AcidSpewerDragon.class})
    void dragonEnteringAfterCastingDoesNotGrantBonus() {
        DragonHunter hunter = new DragonHunter();
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new SilumgarsScorn()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, hunter.getId());
        harness.addToBattlefield(player2, new AcidSpewerDragon());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragon Hunter");
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class})
    void cannotRevealANondragonForTheAdditionalCost() {
        DragonHunter hunter = new DragonHunter();
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new SilumgarsScorn(), new DragonHunter()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player2, 0, hunter.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({SilumgarsScorn.class, DragonHunter.class, AcidSpewerDragon.class, Reverberate.class})
    void copyRetainsBonusFromRevealingDragonForOriginalSpell() {
        DragonHunter hunter = new DragonHunter();
        SilumgarsScorn scorn = new SilumgarsScorn();
        harness.setHand(player1, List.of(hunter));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(scorn, new AcidSpewerDragon(), new Reverberate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.castInstantWithDiscard(player2, 0, hunter.getId(), 1);
        harness.castAndResolveInstant(player2, 1, scorn.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragon Hunter");
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(scorn.getId()));
    }
}

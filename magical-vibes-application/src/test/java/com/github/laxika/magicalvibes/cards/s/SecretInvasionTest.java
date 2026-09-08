package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecretInvasion.class, Demystify.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class SecretInvasionTest extends BaseCardTest {

    @Test
    void exilesTargetCreatureAndCopiesItWithTheEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castAndResolveSecretInvasion(enchanted, target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getOriginalCard());
        assertThat(enchanted.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
    }

    @Test
    void cannotChooseTheEnchantedCreatureAsTheExileTarget() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castSecretInvasion(enchanted);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(enchanted.getId());
    }

    @Test
    void exiledCreatureReturnsAndCopyRevertsWhenAuraLeaves() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castAndResolveSecretInvasion(enchanted, target);

        Permanent aura = findPermanent(player1, "Secret Invasion");
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(target.getOriginalCard().getId()));
        assertThat(enchanted.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void enchantedCreatureHasWardTwo() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castAndResolveSecretInvasion(enchanted, target);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(enchanted.getMarkedDamage()).isZero();
    }

    private void castSecretInvasion(Permanent enchanted) {
        harness.setHand(player1, List.of(new SecretInvasion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
    }

    private void castAndResolveSecretInvasion(Permanent enchanted, Permanent target) {
        castSecretInvasion(enchanted);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}

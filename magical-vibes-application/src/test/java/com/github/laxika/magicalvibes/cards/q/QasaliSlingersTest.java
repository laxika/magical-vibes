package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IngeniousLeonin;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QasaliSlingers.class, IngeniousLeonin.class, GrizzlyBears.class,
        LeoninScimitar.class, RuleOfLaw.class})
class QasaliSlingersTest extends BaseCardTest {

    @Test
    @DisplayName("A Cat entering may destroy a target artifact")
    void catEntryDestroysArtifact() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());

        castCat();
        chooseTarget("Leonin Scimitar");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("A Cat entering may destroy a target enchantment")
    void catEntryDestroysEnchantment() {
        addSlingers();
        harness.addToBattlefield(player2, new RuleOfLaw());

        castCat();
        chooseTarget("Rule of Law");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Rule of Law");
    }

    @Test
    @DisplayName("The destruction may be declined")
    void destructionMayBeDeclined() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());

        castCat();
        chooseTarget("Leonin Scimitar");
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("A non-Cat entering does not trigger the ability")
    void nonCatEntryDoesNotTrigger() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("The trigger cannot target a creature")
    void triggerCannotTargetCreature() {
        addSlingers();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCat();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Qasali Slingers triggers when it enters")
    void selfEntryTriggers() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, java.util.List.of(new QasaliSlingers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    private void addSlingers() {
        harness.addToBattlefield(player1, new QasaliSlingers());
    }

    private void castCat() {
        harness.setHand(player1, List.of(new IngeniousLeonin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseTarget(String cardName) {
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, cardName));
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientGreenwarden;
import com.github.laxika.magicalvibes.cards.e.ExpeditionChampion;
import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynchronizedSpellcraft.class, AncientGreenwarden.class, ExpeditionChampion.class,
        ExpeditionDiviner.class, ExpeditionHealer.class, SneakingGuide.class, StoneworkPackbeast.class})
class SynchronizedSpellcraftTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to the target creature and no damage to its controller with no party")
    void dealsBaseDamageWithNoParty() {
        Permanent target = addTarget();

        castSpell(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage to the target creature's controller equal to the party size")
    void dealsPartyDamageToTargetController() {
        Permanent target = addTarget();
        addFullParty();

        castSpell(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Counts the party when the spell resolves")
    void countsPartyAtResolution() {
        Permanent target = addTarget();
        harness.setHand(player1, List.of(new SynchronizedSpellcraft()));
        addMana();
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, target.getId());
        addFullParty();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void duplicateRolesCountOnlyOnce() {
        Permanent target = addTarget();
        harness.addToBattlefield(player1, new SneakingGuide());
        harness.addToBattlefield(player1, new SneakingGuide());
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player2, new ExpeditionHealer());
        harness.addToBattlefield(player2, new ExpeditionChampion());
        harness.addToBattlefield(player2, new ExpeditionDiviner());

        castSpell(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void oneCreatureWithAllPartyTypesCountsAsOne() {
        Permanent target = addTarget();
        harness.addToBattlefield(player1, new StoneworkPackbeast());

        castSpell(target);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void flexiblePartyMemberFillsTheMissingRole() {
        Permanent target = addTarget();
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new SneakingGuide());
        harness.addToBattlefield(player1, new ExpeditionChampion());

        castSpell(target);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void lethalCreatureDamageStillDamagesItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SneakingGuide());
        addFullParty();

        castSpell(target);

        harness.assertInGraveyard(player2, "Sneaking Guide");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void targetingOwnPartyMemberCountsItBeforeLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SneakingGuide());
        harness.setLife(player1, 20);

        castSpell(target);

        harness.assertInGraveyard(player1, "Sneaking Guide");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void vanishedTargetPreventsControllerDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SneakingGuide());
        addFullParty();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SynchronizedSpellcraft(), new SynchronizedSpellcraft()));
        addMana();
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Sneaking Guide");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTarget() {
        return harness.addToBattlefieldAndReturn(player2, new AncientGreenwarden());
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new SneakingGuide());
        harness.addToBattlefield(player1, new ExpeditionChampion());
        harness.addToBattlefield(player1, new ExpeditionDiviner());
    }

    private void castSpell(Permanent target) {
        harness.setHand(player1, List.of(new SynchronizedSpellcraft()));
        addMana();
        harness.setLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysidianElder.class, GrizzlyBears.class, Shock.class})
class MysidianElderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and creates a Wizard token")
    void createsWizardTokenOnEntry() {
        castMysidianElder();

        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(wizard.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The Wizard token deals damage when you cast a noncreature spell")
    void wizardTokenDamagesEachOpponentForNoncreatureSpell() {
        castMysidianElder();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Wizard token does not trigger for a creature spell")
    void wizardTokenDoesNotTriggerForCreatureSpell() {
        castMysidianElder();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Creates exactly one untapped black 0/1 Wizard creature token")
    void createsCorrectTokenBlueprint() {
        castMysidianElder();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(wizard.isTapped()).isFalse();
        assertThat(wizard.getCard().getPower()).isZero();
        assertThat(wizard.getCard().getToughness()).isEqualTo(1);
        assertThat(wizard.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(wizard.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wizard.getCard().getSubtypes()).containsExactly(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("The opponent's noncreature spells do not trigger the Wizard")
    void opponentsSpellDoesNotTriggerWizard() {
        castMysidianElder();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Wizard triggers for each noncreature spell and is the damage source")
    void triggersForEverySpellWithTokenAsSource() {
        castMysidianElder();
        Permanent wizard = findPermanent(player1, "Wizard");
        Permanent elder = findPermanent(player1, "Mysidian Elder");

        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        assertThat(gd.damageDealtThisTurnBySource.get(wizard.getId())).isEqualTo(2);
        assertThat(gd.damageDealtThisTurnBySource).doesNotContainKey(elder.getId());
    }

    @Test
    @DisplayName("The Wizard keeps its ability after Mysidian Elder dies")
    void tokenAbilitySurvivesElderDeath() {
        castMysidianElder();
        Permanent elder = findPermanent(player1, "Mysidian Elder");

        for (int i = 0; i < 2; i++) {
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.castInstant(player2, 0, elder.getId());
            harness.passBothPriorities();
        }
        harness.assertNotOnBattlefield(player1, "Mysidian Elder");
        harness.assertOnBattlefield(player1, "Wizard");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroying the Wizard does not remove its already triggered ability")
    void triggerResolvesAfterTokenDies() {
        castMysidianElder();
        Permanent wizard = findPermanent(player1, "Wizard");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wizard.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Wizard");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    private void castMysidianElder() {
        harness.castFromHand(player1, new MysidianElder(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BloodlineNecromancer;
import com.github.laxika.magicalvibes.cards.d.DireFleetRavager;
import com.github.laxika.magicalvibes.cards.i.IndulgentAristocrat;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterOfDarkRites.class, BloodlineNecromancer.class, DireFleetRavager.class, IndulgentAristocrat.class, VillageRites.class})
class MasterOfDarkRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature adds three black subtype-restricted mana")
    void sacrificesAnotherCreatureForRestrictedMana() {
        Permanent master = addMasterAndFodder();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(master.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(pool.get(ManaColor.BLACK)).isZero();
        assertThat(pool.getSubtypeSpellOnlyManaForColor(
                Set.of(CardSubtype.VAMPIRE, CardSubtype.CLERIC, CardSubtype.DEMON), ManaColor.BLACK))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Restricted mana casts Vampire, Cleric, and Demon spells")
    void restrictedManaCastsAllowedSubtypes() {
        addMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);

        for (CardSubtype subtype : List.of(CardSubtype.VAMPIRE, CardSubtype.CLERIC, CardSubtype.DEMON)) {
            harness.setHand(player1, List.of(creature("Test " + subtype, subtype)));
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Restricted mana cannot cast an unrelated creature spell")
    void restrictedManaRejectsOtherSubtypes() {
        addMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(creature("Test Bear", CardSubtype.BEAR)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot sacrifice the source itself")
    void requiresAnotherCreature() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfDarkRites());
        master.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaPaysGenericAndColoredCostsOfAVampireSpell() {
        addRealMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new BloodlineNecromancer()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void restrictedManaCannotPayForAnUnrelatedCreatureEvenWithEnoughTotalMana() {
        addRealMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new DireFleetRavager()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void restrictedManaCannotPayForAnInstant() {
        Permanent master = addRealMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new VillageRites()));

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, master.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaCannotPayForAVampiresActivatedAbility() {
        addRealMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new IndulgentAristocrat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickMasterCannotActivate() {
        harness.addToBattlefield(player1, new MasterOfDarkRites());
        harness.addToBattlefield(player1, new IndulgentAristocrat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void tappedMasterCannotActivateAgain() {
        addRealMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new IndulgentAristocrat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    void opponentsCreatureCannotPayTheSacrificeCost() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfDarkRites());
        master.setSummoningSick(false);
        harness.addToBattlefield(player2, new IndulgentAristocrat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Indulgent Aristocrat");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void choosingFodderResolvesTheManaAbilityWithoutUsingTheStack() {
        Permanent master = addRealMasterAndFodder();
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new IndulgentAristocrat());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(master.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chosen);
        harness.assertInGraveyard(player1, "Indulgent Aristocrat");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    private Permanent addRealMasterAndFodder() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfDarkRites());
        master.setSummoningSick(false);
        harness.addToBattlefield(player1, new IndulgentAristocrat());
        return master;
    }

    private Permanent addMasterAndFodder() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfDarkRites());
        master.setSummoningSick(false);
        harness.addToBattlefield(player1, creature("Sacrifice Fodder", CardSubtype.BEAR));
        return master;
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}

package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HanweirBattlementsTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T} grants haste to a target creature")
    void grantsHaste() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Meld ability exiles both halves and melds into Hanweir, the Writhing Township")
    void meldsWithGarrison() {
        Permanent battlements = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, namedGarrison());
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(battlements.getId()) || p.getId().equals(garrison.getId()));
        Permanent melded = findPermanent(player1, "Hanweir, the Writhing Township");
        assertThat(melded.getCard()).isInstanceOf(HanweirTheWrithingTownship.class);
        assertThat(melded.getMeldComponentCards()).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Meld ability does nothing without an owned Hanweir Garrison")
    void doesNothingWithoutPartner() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        harness.addToBattlefield(player2, namedGarrison());
        addMeldMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hanweir Battlements");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
    }

    private void addMeldMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private static Card namedGarrison() {
        Card partner = new Card();
        partner.setName("Hanweir Garrison");
        partner.setType(CardType.CREATURE);
        partner.setPower(2);
        partner.setToughness(3);
        return partner;
    }
}

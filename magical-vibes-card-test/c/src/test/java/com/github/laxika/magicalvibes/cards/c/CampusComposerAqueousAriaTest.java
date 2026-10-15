package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CampusComposerAqueousAria.class, Shock.class})
class CampusComposerAqueousAriaTest extends BaseCardTest {

    @Test
    @DisplayName("Campus Composer is prepared immediately on entry without a triggered ability")
    void entersAlreadyPrepared() {
        harness.setHand(player1, List.of(new CampusComposerAqueousAria()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Campus Composer").isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering the battlefield prepares Campus Composer and exiles a castable Aqueous Aria copy")
    void entersPrepared() {
        Permanent composer = castCampusComposer();

        assertThat(composer.isPrepared()).isTrue();
        UUID copyId = composer.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting the prepared Aqueous Aria copy unprepares Campus Composer and creates an Elemental token")
    void castingPrepareCopyUnpreparesAndResolvesSpell() {
        Permanent composer = castCampusComposer();
        UUID copyId = composer.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(composer.isPrepared()).isFalse();
        assertThat(composer.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
    }

    @Test
    @DisplayName("When prepared Campus Composer leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent composer = castCampusComposer();
        UUID copyId = composer.getPreparedSpellCardId();
        assertThat(gd.findExiledCard(copyId)).isNotNull();

        composer.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(composer);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castCampusComposer() {
        harness.setHand(player1, List.of(new CampusComposerAqueousAria()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Campus Composer");
    }

    @Test
    @DisplayName("Casting Aqueous Aria unprepares its source before the spell resolves")
    void unpreparesDuringCasting() {
        Permanent composer = castCampusComposer();
        UUID copyId = composer.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castFromExile(player1, copyId);

        assertThat(composer.isPrepared()).isFalse();
        assertThat(composer.getPreparedSpellCardId()).isNull();
        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Aqueous Aria");
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay two mana")
    void wardCountersUnpaidSpell() {
        Permanent composer = castCampusComposer();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, composer.getId());
        resolveAllTriggers();

        assertThat(composer.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying ward's two mana lets the opponent's spell resolve")
    void wardCanBePaid() {
        Permanent composer = castCampusComposer();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, composer.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(composer.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CampusComposerAqueousAriaTest extends BaseCardTest {

    @Test
    @DisplayName("Has ON_ENTER_BATTLEFIELD BecomePreparedEffect and Aqueous Aria back face")
    void hasCorrectStructure() {
        CampusComposerAqueousAria card = new CampusComposerAqueousAria();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(BecomePreparedEffect.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("AqueousAria");
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard().getName()).isEqualTo("Aqueous Aria");
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

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elemental"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
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
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB BecomePrepared trigger

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Campus Composer"))
                .findFirst()
                .orElseThrow();
    }
}

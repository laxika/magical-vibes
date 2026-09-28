package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "YDFT", collectorNumber = "29")
public class MasterpieceVault extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Champion's Helm",
            "Lightning Greaves",
            "Sword of Body and Mind",
            "Sword of Feast and Famine",
            "Sword of Fire and Ice",
            "Sword of Light and Shadow",
            "Sword of War and Peace");

    private static final LibrarySelectionFollowUp ATTACHMENT_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect createEffect(List<UUID> selectedPermanentIds) {
            return new QueueReflexiveAbilityEffect(
                    new AttachSelectedEquipmentToTargetCreatureEffect(selectedPermanentIds.getFirst()), true);
        }

        @Override
        public String prompt() {
            return "Attach that Equipment to up to one target creature you control.";
        }

        @Override
        public boolean optional() {
            return false;
        }
    };

    public MasterpieceVault() {
        addEffect(EffectSlot.ON_DEATH,
                DraftCardFromSpellbookEffect.toBattlefield(SPELLBOOK, ATTACHMENT_FOLLOW_UP));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new SacrificeSelfCost()),
                "{5}: Sacrifice this artifact."));
    }
}

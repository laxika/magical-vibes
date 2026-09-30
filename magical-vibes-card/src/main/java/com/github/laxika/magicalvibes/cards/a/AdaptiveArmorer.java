package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "YECL", collectorNumber = "1")
public class AdaptiveArmorer extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Amorphous Axe",
            "Citizen's Crowbar",
            "Cloudsteel Kirin",
            "Conqueror's Flail",
            "Fireshrieker",
            "Fishing Pole",
            "Krovod Haunch",
            "Leech Gauntlet",
            "Lion Sash",
            "Mace of the Valiant",
            "Maul of the Skyclaves",
            "Shield of the Realm",
            "Sigiled Sword of Valeron",
            "Thran Power Suit",
            "Thunder Lasso");

    private static final LibrarySelectionFollowUp ATTACHMENT_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect createEffect(List<UUID> selectedPermanentIds) {
            return new QueueReflexiveAbilityEffect(
                    new AttachSelectedEquipmentToTargetCreatureEffect(selectedPermanentIds.getFirst()));
        }

        @Override
        public String prompt() {
            return "Attach that Equipment to target creature you control.";
        }

        @Override
        public boolean optional() {
            return false;
        }
    };

    public AdaptiveArmorer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                DraftCardFromSpellbookEffect.toBattlefield(SPELLBOOK, ATTACHMENT_FOLLOW_UP));
    }
}

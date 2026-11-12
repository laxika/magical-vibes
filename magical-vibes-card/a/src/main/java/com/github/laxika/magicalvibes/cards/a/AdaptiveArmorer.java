package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "1")
public class AdaptiveArmorer extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("MH1", "219"),
            new DraftFromSpellbookEffect.SpellbookCard("SNC", "8"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "8"),
            new DraftFromSpellbookEffect.SpellbookCard("C16", "53"),
            new DraftFromSpellbookEffect.SpellbookCard("MRD", "171"),
            new DraftFromSpellbookEffect.SpellbookCard("FDN", "128"),
            new DraftFromSpellbookEffect.SpellbookCard("MKM", "21"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "106"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "26"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "314"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "27"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "228"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "244"),
            new DraftFromSpellbookEffect.SpellbookCard("BRO", "253"),
            new DraftFromSpellbookEffect.SpellbookCard("OTJ", "35"));

    private static final LibrarySelectionFollowUp ATTACHMENT_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect effect() {
            return QueueReflexiveAbilityEffect.aboutPickedPermanent(
                    new AttachCreatedEquipmentToTargetCreatureEffect());
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
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DraftCardFromSpellbookEffect.toBattlefield(SPELLBOOK, ATTACHMENT_FOLLOW_UP));
    }
}

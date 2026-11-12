package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "29")
public class MasterpieceVault extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("CMD", "244"),
            new DraftFromSpellbookEffect.SpellbookCard("MRD", "199"),
            new DraftFromSpellbookEffect.SpellbookCard("SOM", "208"),
            new DraftFromSpellbookEffect.SpellbookCard("MBS", "138"),
            new DraftFromSpellbookEffect.SpellbookCard("DST", "148"),
            new DraftFromSpellbookEffect.SpellbookCard("DST", "149"),
            new DraftFromSpellbookEffect.SpellbookCard("NPH", "161"));

    private static final LibrarySelectionFollowUp ATTACHMENT_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect effect() {
            return QueueReflexiveAbilityEffect.aboutPickedPermanent(
                    new AttachCreatedEquipmentToTargetCreatureEffect(), true);
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
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                DraftCardFromSpellbookEffect.toBattlefield(SPELLBOOK, ATTACHMENT_FOLLOW_UP));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new SacrificeSelfEffect()),
                "{5}: Sacrifice this artifact."));
    }
}

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.*;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarFile;

/**
 * EffectSlot refactor step 8: lists every card's ON_ENTER_BATTLEFIELD registrations, marking each one that
 * {@code EtbTriggerService} filtered out of the enters trigger as an entry replacement (R) or kept as a trigger (T),
 * plus the static effects of the same classes. Input for {@code step8-entry-replacements.py --scan}.
 *
 * <p>Run it on the card jars of the revision before step 8 (cards are built without oracle data):
 * {@code javac -d out -cp CP scripts/step8-etb-scan.java && java -cp out:CP EtbScan CARD_JAR... > scan.tsv},
 * where CP holds the domain jar and every card jar.
 */
public class EtbScan {

    static boolean isEntryReplacement(CardEffect e, Card card) {
        if (e instanceof ReplacementEffect) return true;
        if (e instanceof BecomePreparedEffect p) return !p.asTriggeredAbility();
        if (e instanceof ConditionalEffect c) return isEntryReplacement(c.wrapped(), card);
        if (e instanceof ConditionalReplacementEffect r) {
            return (r.baseEffect() == null || isEntryReplacement(r.baseEffect(), card))
                    && (r.upgradedEffect() == null || isEntryReplacement(r.upgradedEffect(), card));
        }
        return e instanceof ChooseColorEffect || e instanceof ChooseCardTypeOnEnterEffect
                || e instanceof ChooseBasicLandTypeOnEnterEffect || e instanceof ChooseCreatureOnEnterEffect
                || e instanceof ChooseEquipmentAttachmentOnEnterEffect || e instanceof ChoosePrimalClayFormOnEnterEffect
                || e instanceof SubtypeChoiceOnEnterEffect || e instanceof ChooseRandomNumberOnEnterEffect
                || e instanceof DraftProteanWarEngineSpellbookEffect
                || (e instanceof DraftCardFromSpellbookToExileEffect && card.hasType(CardType.LAND))
                || e instanceof ChooseModeOnEnterEffect || e instanceof ChooseIndependentModesOnEnterEffect;
    }

    static String describe(CardEffect e) {
        if (e instanceof ConditionalEffect c) return "ConditionalEffect(" + describe(c.wrapped()) + ")";
        if (e instanceof ConditionalReplacementEffect r) return "ConditionalReplacementEffect("
                + (r.baseEffect() == null ? "-" : describe(r.baseEffect())) + "," + (r.upgradedEffect() == null ? "-" : describe(r.upgradedEffect())) + ")";
        return e.getClass().getSimpleName();
    }

    public static void main(String[] args) throws Exception {
        for (String jarPath : args) {
            String module = new File(jarPath).getName().replace("magical-vibes-card-", "").replace(".jar", "");
            try (JarFile jar = new JarFile(jarPath)) {
                List<String> names = jar.stream().map(x -> x.getName())
                        .filter(n -> n.endsWith(".class") && n.contains("/cards/") && !n.contains("$")).sorted().toList();
                for (String n : names) {
                    String cn = n.replace('/', '.').replace(".class", "");
                    Class<?> cls;
                    try { cls = Class.forName(cn); } catch (Throwable t) { continue; }
                    if (!Card.class.isAssignableFrom(cls) || Modifier.isAbstract(cls.getModifiers())) continue;
                    Card card;
                    try { card = (Card) cls.getDeclaredConstructor().newInstance(); }
                    catch (Throwable t) { System.out.println("ERR\t" + module + "\t" + cls.getSimpleName() + "\t" + t); continue; }
                    List<EffectRegistration> regs = card.getEffectRegistrations(EffectSlot.ON_ENTER_BATTLEFIELD);
                    for (int i = 0; i < regs.size(); i++) {
                        EffectRegistration r = regs.get(i);
                        boolean repl = isEntryReplacement(r.effect(), card);
                        System.out.println("ETB\t" + module + "\t" + cls.getSimpleName() + "\t" + i + "\t" + (repl ? "R" : "T")
                                + "\t" + describe(r.effect()) + "\t" + card.getEffectTargetIndex(r.effect())
                                + "\t" + (r.simultaneousWithPrevious() ? "SIM" : "-") + "\t" + r.triggerMode());
                    }
                    for (CardEffect e : card.getStaticEffects()) {
                        if (isEntryReplacement(e, card)) {
                            System.out.println("STATIC\t" + module + "\t" + cls.getSimpleName() + "\t" + describe(e));
                        }
                    }
                }
            }
        }
    }
}

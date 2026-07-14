package com.github.laxika.magicalvibes.architecture;

import com.github.laxika.magicalvibes.model.Card;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Build-time backstop for the Card immutability invariant (runtime counterpart:
 * {@code Card.freeze()}): live Card objects are shared between the real game and AI simulation
 * copies, so mutating one from game logic leaks state across games. Only classes that assemble
 * <em>freshly created</em> Card instances (token/copy creation, game setup, the modal
 * copy-on-cast) may call Card mutators.
 */
class CardImmutabilityArchTest {

    private static final Pattern MUTATOR_NAMES = Pattern.compile(
            "set[A-Z]\\w*|add[A-Z]\\w*|target|registerEffectTargetIndex|clearRuntimeSpellTargets"
                    + "|copyTargetingFrom|removeKeyword");

    /**
     * Classes allowed to call Card mutators. Each either mutates a Card it just created
     * ({@code new Card()} / {@code createRuntimeCopy()}) or runs before the card is frozen
     * (game setup, card constructors). Before adding a class here, confirm the instance it
     * mutates can never be a live, shared card.
     */
    private static final Set<String> WHITELISTED_CLASSES = Set.of(
            "Card",                    // its own builder API
            "SpellTarget",             // construction-time builder callback into Card
            "GameSetupService",        // stamps ownerId, then freezes
            "SpellCastingService",     // mutates the modal runtime copy it just created
            "AiDecisionEngine",        // mutates the evaluation runtime copy it just created
            "PermanentCopierService",  // assembles fresh clone-copy cards
            "CloneService",            // "except it has ..." on the fresh clone-copy card
            "CopySupport",             // assembles fresh spell-copy cards
            "CopySpellEffectHandler",  // decorates the fresh spell-copy card
            "MayCopyHandlerService",   // re-adds the copy ability on the fresh clone-copy card
            "BecomeCopyOfDyingCreatureEffectHandler", // "except it has this ability" on the fresh clone-copy card (Cemetery Puca)
            "DestructionSupport",
            "GraveyardReturnSupport",
            "PermanentControlSupport",
            "LivingWeaponEffectHandler",
            "ExileCreaturesFromGraveyardAndCreateTokensEffectHandler",
            "DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffectHandler");

    private static boolean isWhitelisted(JavaClass javaClass) {
        String outermost = javaClass.getName().replaceAll("\\$.*", "");
        String simpleName = outermost.substring(outermost.lastIndexOf('.') + 1);
        return WHITELISTED_CLASSES.contains(simpleName)
                || simpleName.startsWith("CreateToken")
                || simpleName.startsWith("CreateLifeTotal")
                || javaClass.getPackageName().contains(".cards");
    }

    @Test
    @DisplayName("Only card-assembly classes may mutate Card objects")
    void onlyCardAssemblyClassesMutateCards() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .withImportOption(location -> !location.contains("test-fixtures"))
                .importPackages("com.github.laxika.magicalvibes");

        noClasses()
                .that(DescribedPredicate.describe("are not whitelisted card-assembly classes",
                        javaClass -> !isWhitelisted(javaClass)))
                .should().callMethodWhere(DescribedPredicate.describe("mutate a Card",
                        (JavaMethodCall call) -> call.getTargetOwner().isAssignableTo(Card.class)
                                && MUTATOR_NAMES.matcher(call.getName()).matches()))
                .because("live Card objects are shared with AI simulation copies — store runtime state on the "
                        + "Permanent, the StackEntry, or GameData (see Card.freeze()). If your class only mutates "
                        + "Card instances it just created, add it to WHITELISTED_CLASSES with a justification")
                .check(classes);
    }
}
